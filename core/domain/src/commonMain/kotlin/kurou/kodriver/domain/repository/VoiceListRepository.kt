package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.TextToSpeechVoice

/** インストール済みの読み上げ音声を取得するRepository。 */
interface VoiceListRepository {
    suspend fun availableVoices(): List<TextToSpeechVoice>
}
