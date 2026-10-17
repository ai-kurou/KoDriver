package kurou.kodriver.core.devicevolumedata.windows

import com.sun.jna.platform.win32.Guid.IID
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JnaWasapiNativeApiTest {
    private val com = FakeWasapiComBridge()
    private val api = JnaWasapiNativeApi(com)

    @Test
    fun `COM初期化の成功と初期化済みをtrueに変換する`() {
        for (result in listOf(0, 1)) {
            com.initializeResult = HRESULT(result)

            assertTrue(api.initialize())
            assertEquals(2, com.initializeFlags)
        }
    }

    @Test
    fun `COM初期化の失敗をfalseに変換する`() {
        com.initializeResult = HRESULT(-2147417850)

        assertFalse(api.initialize())
        assertEquals(2, com.initializeFlags)
    }

    @Test
    fun `COM終了を境界へ委譲する`() {
        api.uninitialize()

        assertEquals(1, com.uninitializeCount)
    }

    @Test
    fun `列挙子を所定のGUIDとコンテキストで生成して返されたポインターを使う`() {
        val enumerator = api.createDeviceEnumerator()
        enumerator.release()

        val creation = requireNotNull(com.creation)
        assertEquals("{BCDE0395-E52F-467C-8E3D-C4579291692E}", creation.first.toGuidString())
        assertEquals(23, creation.second)
        assertEquals("{A95664D2-9614-4F35-A746-DE8DB63617E6}", creation.third.toGuidString())
        assertEquals(listOf(com.enumeratorPointer), com.releasedPointers)
        assertEquals(listOf(0), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `既定の再生デバイスを所定のvtable番号とロールで取得する`() {
        val device = api.createDeviceEnumerator().getDefaultAudioEndpoint()
        device.release()

        val call = com.invocations.single()
        assertEquals(com.enumeratorPointer, call.pointer)
        assertEquals(4, call.index)
        assertEquals(listOf(com.enumeratorPointer, 0, 1), call.arguments.take(3))
        assertEquals(4, call.arguments.size)
        assertIs<PointerByReference>(call.arguments[3])
        assertEquals(listOf(com.devicePointer), com.releasedPointers)
        assertEquals(listOf(0, 0), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `音量インターフェースを所定のIIDとvtable番号で有効化する`() {
        val volume = api.createDeviceEnumerator().getDefaultAudioEndpoint().activate()
        volume.release()

        val call = com.invocations.last()
        assertEquals(com.devicePointer, call.pointer)
        assertEquals(3, call.index)
        assertEquals(com.devicePointer, call.arguments[0])
        assertEquals("{5CDF2C82-841E-4546-9722-0CF74078229A}", assertIs<IID>(call.arguments[1]).toGuidString())
        assertEquals(23, call.arguments[2])
        assertEquals(null, call.arguments[3])
        assertIs<PointerByReference>(call.arguments[4])
        assertEquals(5, call.arguments.size)
        assertEquals(listOf(com.volumePointer), com.releasedPointers)
        assertEquals(listOf(0, 0, 0), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `音量取得は所定のvtableを呼び出して出力引数の値を返す`() {
        val volume = api.createDeviceEnumerator().getDefaultAudioEndpoint().activate()

        assertEquals(0.75f, volume.getMasterVolumeLevelScalar())

        val call = com.invocations.last()
        assertEquals(com.volumePointer, call.pointer)
        assertEquals(9, call.index)
        assertEquals(com.volumePointer, call.arguments[0])
        assertIs<FloatByReference>(call.arguments[1])
        assertEquals(2, call.arguments.size)
        assertEquals(listOf(0, 0, 0, 0), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `音量設定は所定のvtableへ設定値とイベントコンテキストを渡す`() {
        val volume = api.createDeviceEnumerator().getDefaultAudioEndpoint().activate()

        volume.setMasterVolumeLevelScalar(0.25f)

        val call = com.invocations.last()
        assertEquals(com.volumePointer, call.pointer)
        assertEquals(7, call.index)
        assertEquals(listOf(com.volumePointer, 0.25f, null), call.arguments)
        assertEquals(listOf(0, 0, 0, 0), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `列挙子生成の失敗は結果確認で例外を伝える`() {
        com.failingCall = "create"

        assertSame(com.failure, assertFailsWith<IllegalStateException> { api.createDeviceEnumerator() })
        assertEquals(listOf(-1), com.checkedResults.map { it.toInt() })
        assertTrue(com.invocations.isEmpty())
    }

    @Test
    fun `デバイス取得の失敗は結果確認で例外を伝える`() {
        val enumerator = api.createDeviceEnumerator()
        com.failingCall = "4"

        assertSame(com.failure, assertFailsWith<IllegalStateException> { enumerator.getDefaultAudioEndpoint() })
        assertEquals(listOf(0, -1), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `有効化の失敗は結果確認で例外を伝える`() {
        val device = api.createDeviceEnumerator().getDefaultAudioEndpoint()
        com.failingCall = "3"

        assertSame(com.failure, assertFailsWith<IllegalStateException> { device.activate() })
        assertEquals(listOf(0, 0, -1), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `音量取得の失敗は出力値を返さず例外を伝える`() {
        val volume = api.createDeviceEnumerator().getDefaultAudioEndpoint().activate()
        com.failingCall = "9"

        assertSame(com.failure, assertFailsWith<IllegalStateException> { volume.getMasterVolumeLevelScalar() })
        assertEquals(listOf(0, 0, 0, -1), com.checkedResults.map { it.toInt() })
    }

    @Test
    fun `音量設定の失敗は結果確認で例外を伝える`() {
        val volume = api.createDeviceEnumerator().getDefaultAudioEndpoint().activate()
        com.failingCall = "7"

        assertSame(com.failure, assertFailsWith<IllegalStateException> { volume.setMasterVolumeLevelScalar(0.25f) })
        assertEquals(listOf(0, 0, 0, -1), com.checkedResults.map { it.toInt() })
    }
}
