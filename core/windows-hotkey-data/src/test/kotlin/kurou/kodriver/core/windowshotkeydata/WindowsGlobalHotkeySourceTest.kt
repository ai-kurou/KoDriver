package kurou.kodriver.core.windowshotkeydata

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WindowsGlobalHotkeySourceTest {
    private val hotkey = GlobalHotkey(virtualKeyCode = GlobalHotkey.VK_F13)

    @Test
    fun `非 Windows では API を生成せず通知なしで完了する`() =
        runTest {
            val source = WindowsGlobalHotkeySource.create("Linux") { error("Native API must not be loaded") }

            assertEquals(emptyList(), source.observe(hotkey).toList())
            assertEquals(emptyList(), WindowsGlobalHotkeySource.create("macOS").observe(hotkey).toList())
            assertTrue(WindowsGlobalHotkeySource.create("Windows 11") is WindowsGlobalHotkeySource)
        }

    @Test
    fun `登録失敗時は通知なしで完了し解除しない`() =
        runTest {
            val fake = FakeHotkeyNativeApi(registrationResult = false)
            val source = WindowsGlobalHotkeySource.create("wInDoWs 11") { fake }

            assertEquals(emptyList(), source.observe(hotkey).toList())
            assertEquals(hotkey, fake.registeredHotkey)
            assertEquals(0, fake.unregisterCount)
            assertEquals(emptyList(), fake.quitThreadIds)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `購読時だけ登録し対象の押下を一度ずつ通知してキャンセル時に解除する`() =
        runTest {
            val fake = FakeHotkeyNativeApi()
            val requested = hotkey.copy(modifiers = 2)
            val flow = WindowsGlobalHotkeySource(fake).observe(requested)
            assertEquals(null, fake.registeredHotkey)
            val result = async { flow.take(2).toList() }
            fake.registered.await()
            fake.send(HotkeyMessage(0, HOTKEY_ID))
            fake.send(HotkeyMessage(WM_HOTKEY, HOTKEY_ID + 1))
            repeat(2) { fake.send(HotkeyMessage(WM_HOTKEY, HOTKEY_ID)) }

            assertEquals(listOf(Unit, Unit), result.await())
            assertEquals(requested, fake.registeredHotkey)
            assertEquals(listOf(FakeHotkeyNativeApi.THREAD_ID), fake.quitThreadIds)
            assertEquals(1, fake.unregisterCount)
            assertSame(fake.registrationThread, fake.unregisterThread)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `キュー準備中のキャンセルも準備完了後に解除してスレッドを終了する`() =
        runTest {
            val fake = FakeHotkeyNativeApi()
            val gate = CompletableDeferred<Unit>()
            fake.preparationGate = gate
            val collection = launch { WindowsGlobalHotkeySource(fake).observe(hotkey).collect() }
            fake.preparing.await()
            collection.cancel()
            yield()
            assertFalse(collection.isCompleted)
            gate.complete(Unit)
            collection.join()

            assertEquals(listOf(FakeHotkeyNativeApi.THREAD_ID), fake.quitThreadIds)
            assertEquals(1, fake.unregisterCount)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `メッセージループ終了時も登録を解除する`() =
        runTest {
            val fake = FakeHotkeyNativeApi()
            fake.finish()

            assertEquals(emptyList(), WindowsGlobalHotkeySource(fake).observe(hotkey).toList())
            assertEquals(1, fake.unregisterCount)
            assertEquals(emptyList(), fake.quitThreadIds)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `メッセージ取得例外時も登録を解除して例外を通知する`() =
        runTest {
            val failure = IllegalStateException("GetMessage failed")
            val fake = FakeHotkeyNativeApi(messageFailure = failure)

            val actual =
                assertFailsWith<IllegalStateException> {
                    WindowsGlobalHotkeySource(fake).observe(hotkey).toList()
                }

            assertEquals(failure.message, actual.message)
            assertEquals(1, fake.unregisterCount)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `キュー準備例外時は登録せず例外を通知する`() =
        runTest {
            val failure = IllegalStateException("Queue preparation failed")
            val fake = FakeHotkeyNativeApi(preparationFailure = failure)

            val actual =
                assertFailsWith<IllegalStateException> {
                    WindowsGlobalHotkeySource(fake).observe(hotkey).toList()
                }

            assertEquals(failure.message, actual.message)
            assertEquals(null, fake.registeredHotkey)
            assertEquals(0, fake.unregisterCount)
            assertEquals(emptyList(), fake.quitThreadIds)
        }

    @Test
    fun `再購読すると新しいスレッドで再登録する`() =
        runTest {
            val fake = FakeHotkeyNativeApi()
            val source = WindowsGlobalHotkeySource(fake)
            fake.finish()
            source.observe(hotkey).toList()
            val firstThread = fake.registrationThread
            fake.finish()
            source.observe(hotkey).toList()

            assertEquals(2, fake.unregisterCount)
            assertFalse(firstThread === fake.registrationThread)
            assertFalse(requireNotNull(firstThread).isAlive)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }

    @Test
    fun `押下のない購読をキャンセルするとスレッドが終了する`() =
        runTest {
            val fake = FakeHotkeyNativeApi()
            val collection = launch { WindowsGlobalHotkeySource(fake).observe(hotkey).collect() }
            fake.registered.await()
            collection.cancelAndJoin()

            assertTrue(collection.isCancelled)
            assertEquals(1, fake.unregisterCount)
            assertEquals(listOf(FakeHotkeyNativeApi.THREAD_ID), fake.quitThreadIds)
            assertSame(fake.registrationThread, fake.unregisterThread)
            assertFalse(requireNotNull(fake.registrationThread).isAlive)
        }
}
