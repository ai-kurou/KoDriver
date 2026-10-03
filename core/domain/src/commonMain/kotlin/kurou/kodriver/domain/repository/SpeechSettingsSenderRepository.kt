package kurou.kodriver.domain.repository

/** Windowsの音声設定を開くRepository。Windows以外では何もしない。 */
interface SpeechSettingsSenderRepository {
    fun openWindowsSpeechSettings()
}
