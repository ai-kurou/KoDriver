package kurou.kodriver.core.texttospeechdata.repository

import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository

/** Windowsの音声設定はAndroidから開けないため何もしないNo-Op実装。 */
internal class AndroidSpeechSettingsSenderRepository : SpeechSettingsSenderRepository {
    override fun openWindowsSpeechSettings() = Unit
}
