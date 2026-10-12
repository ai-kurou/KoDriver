package kurou.kodriver.core.windowshotkeydata

import kotlinx.coroutines.flow.Flow

/** 購読中だけホットキーを登録し、押下ごとに一度通知する。 */
interface GlobalHotkeySource {
    fun observe(hotkey: GlobalHotkey): Flow<Unit>
}
