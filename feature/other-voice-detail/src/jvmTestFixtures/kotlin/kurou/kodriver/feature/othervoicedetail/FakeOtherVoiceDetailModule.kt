package kurou.kodriver.feature.othervoicedetail

import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository
import org.koin.dsl.module

/** テスト用の音声一覧を登録し、Windows の SAPI への呼び出しを避ける。 */
val fakeOtherVoiceDetailModule =
    module {
        single<VoiceListRepository> { FakeVoiceListRepository() }
    }

class FakeVoiceListRepository : VoiceListRepository {
    override suspend fun availableVoices(): List<TextToSpeechVoice> =
        listOf(TextToSpeechVoice("test-voice", "テスト音声", "ja-JP"))
}
