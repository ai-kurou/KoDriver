package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.SpeechSynthesizer
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository

/** 再読み込みで音声の追加・削除や既定音声の変更を反映するため、OSの音声一覧を毎回排他的に取得する。 */
internal class DesktopVoiceListRepository(
    private val synthesizer: SpeechSynthesizer = SapiSpeechSynthesizer(),
) : VoiceListRepository {
    private val mutex = Mutex()

    override suspend fun availableVoices(): List<TextToSpeechVoice> =
        mutex.withLock {
            withContext(Dispatchers.IO) { synthesizer.listVoices() }
        }
}
