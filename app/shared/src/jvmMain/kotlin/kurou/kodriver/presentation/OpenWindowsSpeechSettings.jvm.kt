package kurou.kodriver.presentation

import io.sentry.Sentry
import java.io.IOException

private const val WINDOWS_SPEECH_SETTINGS_URI = "ms-settings:speech"

/**
 * Windowsでは音声設定を開き、それ以外のJVM環境では何もしない。
 *
 * `Desktop.browse` は環境によって `ms-settings:` を扱えず例外になるため、`rundll32` 経由で開く。
 * 起動に失敗してもアプリを落とさず、原因調査のためSentryへ記録する。
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
    captureException: (Throwable) -> Unit = Sentry::captureException,
) {
    if (!isWindows) return
    try {
        startProcess(listOf("rundll32", "url.dll,FileProtocolHandler", WINDOWS_SPEECH_SETTINGS_URI))
    } catch (e: IOException) {
        // 設定画面を開けなくてもアプリの動作には影響しないため、記録のみ行う。
        captureException(e)
    } catch (e: SecurityException) {
        captureException(e)
    }
}
