package kurou.kodriver.core.windowshotkeydata

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser.MSG

private const val WM_QUIT = 0x0012

/**
 * JNA の user32 API を直接呼び出す Windows 専用実装。
 * プラットフォーム固有の外部 API を直接呼び出すためユニットテスト対象外とし、
 * [WindowsGlobalHotkeySource] のロジックを [HotkeyNativeApi] 経由の Fake でテストする。
 */
internal class JnaHotkeyNativeApi : HotkeyNativeApi {
    override fun prepareMessageQueue(): Int {
        User32.INSTANCE.PeekMessage(MSG(), null, 0, 0, 0)
        return Kernel32.INSTANCE.GetCurrentThreadId()
    }

    override fun register(hotkey: GlobalHotkey): Int {
        val registered =
            User32.INSTANCE.RegisterHotKey(null, HOTKEY_ID, hotkey.modifiers or MOD_NOREPEAT, hotkey.virtualKeyCode)
        return if (registered) 0 else Kernel32.INSTANCE.GetLastError()
    }

    override fun nextMessage(): HotkeyMessage? {
        val message = MSG()
        val result = User32.INSTANCE.GetMessage(message, null, 0, 0)
        check(result != -1) { "GetMessage failed: ${Kernel32.INSTANCE.GetLastError()}" }
        if (result == 0) return null
        return HotkeyMessage(message.message, message.wParam.toInt())
    }

    override fun unregister() {
        User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID)
    }

    override fun postQuit(threadId: Int) {
        check(User32.INSTANCE.PostThreadMessage(threadId, WM_QUIT, WPARAM(0), LPARAM(0)) != 0) {
            "PostThreadMessage failed: ${Kernel32.INSTANCE.GetLastError()}"
        }
    }
}
