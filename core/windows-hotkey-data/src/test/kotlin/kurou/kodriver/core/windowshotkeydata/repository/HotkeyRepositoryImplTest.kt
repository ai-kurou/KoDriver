package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kotlin.test.Test
import kotlin.test.assertEquals

class HotkeyRepositoryImplTest {
    @Test
    fun `修飾キーなしの F9 固定で押下を通知する`() =
        runTest {
            val requested = mutableListOf<GlobalHotkey>()
            val source = RecordingSource(flowOf(Unit, Unit), requested)

            assertEquals(listOf(Unit, Unit), HotkeyRepositoryImpl(source).hotkeyPressStream().toList())
            assertEquals(listOf(GlobalHotkey(virtualKeyCode = 0x78)), requested)
            assertEquals(0x78, GlobalHotkey.VK_F9)
        }

    @Test
    fun `Source が空なら通知しない`() =
        runTest {
            val requested = mutableListOf<GlobalHotkey>()

            val repository = HotkeyRepositoryImpl(RecordingSource(emptyFlow(), requested))

            assertEquals(emptyList(), repository.hotkeyPressStream().toList())
            assertEquals(listOf(GlobalHotkey(virtualKeyCode = GlobalHotkey.VK_F9)), requested)
        }
}

private class RecordingSource(
    private val presses: Flow<Unit>,
    private val requested: MutableList<GlobalHotkey>,
) : GlobalHotkeySource {
    override fun observe(hotkey: GlobalHotkey): Flow<Unit> {
        requested.add(hotkey)
        return presses
    }
}
