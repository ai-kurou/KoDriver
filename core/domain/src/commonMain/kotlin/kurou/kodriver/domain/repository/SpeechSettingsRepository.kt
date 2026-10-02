package kurou.kodriver.domain.repository

/** Windowsの音声設定を開くRepository。Windows以外では何もしない。 */
interface SpeechSettingsRepository {
    fun openWindowsSpeechSettings()
}
