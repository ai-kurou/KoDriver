package kurou.kodriver.core.texttospeechdata.repository

import io.sentry.Sentry
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import java.io.IOException

private const val WINDOWS_SPEECH_SETTINGS_URI = "ms-settings:speech"

/**
 * Windowsでは音声設定を開き、それ以外のJVM環境では何もしない。
 *
 * `Desktop.browse` は環境によって `ms-settings:` を扱えず例外になるため、`rundll32` 経由で開く。
 * 起動に失敗してもアプリを落とさず、原因調査のためSentryへ記録する。
 */
internal class WindowsSpeechSettingsSenderRepository(
    private val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows", ignoreCase = true),
    private val startProcess: (List<String>) -> Unit = { command -> ProcessBuilder(command).start() },
    private val captureException: (Throwable) -> Unit = Sentry::captureException,
) : SpeechSettingsSenderRepository {
    override fun openWindowsSpeechSettings() {
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
}
