package kurou.kodriver.presentation

import java.io.IOException

private const val WINDOWS_SPEECH_SETTINGS_URI = "ms-settings:speech"

/**
 * Windowsでは音声設定を開き、それ以外のJVM環境では何もしない。
 *
 * `Desktop.browse` は環境によって `ms-settings:` を扱えず例外になるため、`rundll32` 経由で開く。
 * 起動に失敗してもアプリを落とさない。
 */
actual fun openWindowsSpeechSettings() {
    openWindowsSpeechSettings(
        isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true),
        startProcess = { command -> ProcessBuilder(command).start() },
    )
}

internal fun openWindowsSpeechSettings(
    isWindows: Boolean,
    startProcess: (List<String>) -> Unit,
) {
    if (!isWindows) return
    try {
        startProcess(listOf("rundll32", "url.dll,FileProtocolHandler", WINDOWS_SPEECH_SETTINGS_URI))
    } catch (_: IOException) {
        // 設定画面を開けなくてもアプリの動作には影響しないため無視する。
    } catch (_: SecurityException) {
        // 同上。
    }
}
