package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.Callback
import com.sun.jna.CallbackReference
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMException
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.PointerByReference
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import java.lang.ref.Reference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JnaWasapiComBridgeTest {
    private val ole32 = mockk<Ole32>()
    private val bridge = JnaWasapiComBridge { ole32 }
    private val nativeBridge = JnaWasapiComBridge()

    @Test
    fun `初期化は予約引数とフラグを渡しHRESULTをそのまま返す`() {
        val result = HRESULT(1)
        every { ole32.CoInitializeEx(null, 2) } returns result

        assertSame(result, bridge.initialize(2))

        verify(exactly = 1) { ole32.CoInitializeEx(null, 2) }
        confirmVerified(ole32)
    }

    @Test
    fun `終了はCOM終了処理を一度呼ぶ`() {
        every { ole32.CoUninitialize() } just runs

        bridge.uninitialize()

        verify(exactly = 1) { ole32.CoUninitialize() }
        confirmVerified(ole32)
    }

    @Test
    fun `生成はGUIDとコンテキストと出力引数を渡しHRESULTを返す`() {
        val classId = CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E")
        val interfaceId = IID("A95664D2-9614-4F35-A746-DE8DB63617E6")
        val output = PointerByReference()
        val pointer = Pointer(123)
        val result = HRESULT(-1)
        every { ole32.CoCreateInstance(classId, null, 23, interfaceId, output) } answers {
            output.value = pointer
            result
        }

        assertSame(result, bridge.createInstance(classId, 23, interfaceId, output))
        assertEquals(pointer, output.value)

        verify(exactly = 1) { ole32.CoCreateInstance(classId, null, 23, interfaceId, output) }
        confirmVerified(ole32)
    }

    @Test
    fun `ネイティブAPIの例外は初期化元へ伝播する`() {
        val failure = IllegalStateException("initialize failed")
        every { ole32.CoInitializeEx(null, 2) } throws failure

        assertSame(failure, assertFailsWith<IllegalStateException> { bridge.initialize(2) })

        verify(exactly = 1) { ole32.CoInitializeEx(null, 2) }
        confirmVerified(ole32)
    }

    @Test
    fun `vtableの指定スロットにポインターと引数を渡しHRESULTを返す`() {
        val calls = mutableListOf<Pair<Pointer, Int>>()
        val callback =
            object : IntArgumentCallback {
                override fun invoke(
                    pointer: Pointer,
                    value: Int,
                ): Int {
                    calls += pointer to value
                    return -1
                }
            }
        Memory(Native.POINTER_SIZE.toLong()).use { instance ->
            Memory(5L * Native.POINTER_SIZE).use { vtable ->
                vtable.clear()
                vtable.setPointer(4L * Native.POINTER_SIZE, CallbackReference.getFunctionPointer(callback))
                instance.setPointer(0, vtable)

                assertEquals(-1, nativeBridge.invoke(instance, 4, arrayOf(instance, 42)).toInt())
                assertEquals(listOf<Pair<Pointer, Int>>(instance to 42), calls)
                Reference.reachabilityFence(callback)
            }
        }
    }

    @Test
    fun `解放はIUnknownのReleaseスロットに対象ポインターを渡す`() {
        val calls = mutableListOf<Pointer>()
        val callback =
            object : ReleaseCallback {
                override fun invoke(pointer: Pointer): Int {
                    calls += pointer
                    return 0
                }
            }
        Memory(Native.POINTER_SIZE.toLong()).use { instance ->
            Memory(3L * Native.POINTER_SIZE).use { vtable ->
                vtable.clear()
                vtable.setPointer(2L * Native.POINTER_SIZE, CallbackReference.getFunctionPointer(callback))
                instance.setPointer(0, vtable)

                nativeBridge.release(instance)

                assertEquals(listOf<Pointer>(instance), calls)
                Reference.reachabilityFence(callback)
            }
        }
    }

    @Test
    fun `成功HRESULTの確認は例外を発生させない`() {
        bridge.checkResult(HRESULT(0))
        bridge.checkResult(HRESULT(1))
    }

    @Test
    fun `失敗HRESULTの確認はCOMUtilsの例外を伝播する`() {
        val result = HRESULT(-1)
        val failure = COMException("failed", result)
        // エラーメッセージの生成がKernel32を使うため、この外部呼び出しだけ差し替える。
        mockkStatic(COMUtils::class)
        try {
            every { COMUtils.checkRC(result) } throws failure

            assertSame(failure, assertFailsWith<COMException> { bridge.checkResult(result) })

            verify(exactly = 1) { COMUtils.checkRC(result) }
            confirmVerified(COMUtils::class)
        } finally {
            unmockkStatic(COMUtils::class)
        }
    }

    private interface IntArgumentCallback : Callback {
        fun invoke(
            pointer: Pointer,
            value: Int,
        ): Int
    }

    private interface ReleaseCallback : Callback {
        fun invoke(pointer: Pointer): Int
    }
}
