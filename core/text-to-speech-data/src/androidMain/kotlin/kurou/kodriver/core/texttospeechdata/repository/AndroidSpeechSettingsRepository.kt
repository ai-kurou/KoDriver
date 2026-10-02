package kurou.kodriver.core.texttospeechdata.repository

import kurou.kodriver.domain.repository.SpeechSettingsRepository

/** Windowsの音声設定はAndroidから開けないため何もしないNo-Op実装。 */
internal class AndroidSpeechSettingsRepository : SpeechSettingsRepository {
    override fun openWindowsSpeechSettings() = Unit
}
