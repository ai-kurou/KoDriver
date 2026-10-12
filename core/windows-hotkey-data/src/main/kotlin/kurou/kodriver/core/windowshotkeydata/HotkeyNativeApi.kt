package kurou.kodriver.core.windowshotkeydata

/** Windows のスレッドメッセージキューへのアクセス。テストでは Fake に差し替える。 */
internal interface HotkeyNativeApi {
    fun prepareMessageQueue(): Int

    fun register(hotkey: GlobalHotkey): Boolean

    fun nextMessage(): HotkeyMessage?

    fun unregister()

    fun postQuit(threadId: Int)
}

internal data class HotkeyMessage(
    val message: Int,
    val hotkeyId: Int,
)

internal const val HOTKEY_ID = 1
internal const val WM_HOTKEY = 0x0312
internal const val MOD_NOREPEAT = 0x4000
