package kurou.kodriver.core.windowshotkeydata

/** Windows の修飾キーのビットマスクと仮想キーコードで指定するグローバルホットキー。 */
internal data class GlobalHotkey(
    val modifiers: Int = 0,
    val virtualKeyCode: Int,
)
