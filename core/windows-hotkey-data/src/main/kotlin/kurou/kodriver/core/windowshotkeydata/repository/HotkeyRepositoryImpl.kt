package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.repository.HotkeyRepository

class HotkeyRepositoryImpl(
    private val source: GlobalHotkeySource,
) : HotkeyRepository {
    override fun hotkeyPressStream(): Flow<Unit> = source.observe(GlobalHotkey(virtualKeyCode = GlobalHotkey.VK_F9))
}
