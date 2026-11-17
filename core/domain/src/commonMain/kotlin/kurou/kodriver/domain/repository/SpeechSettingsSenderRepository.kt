package kurou.kodriver.domain.repository

/** Windowsの音声設定を開くRepository。Windows以外では何もしない。 */
public interface SpeechSettingsSenderRepository {
    public fun openWindowsSpeechSettings()
}
