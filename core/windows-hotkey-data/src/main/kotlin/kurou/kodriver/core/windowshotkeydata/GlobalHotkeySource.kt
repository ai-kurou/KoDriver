package kurou.kodriver.core.windowshotkeydata

import kotlinx.coroutines.flow.Flow

/** 購読中だけホットキーを登録し、登録結果と押下を通知する。 */
interface GlobalHotkeySource {
    fun observe(hotkey: GlobalHotkey): Flow<GlobalHotkeyEvent>
}
