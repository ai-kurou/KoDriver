package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository

/** Windowsの音声一覧を排他的に取得し、非空の結果を保持する。空の場合は次回の呼び出しで再取得する。 */
internal class WindowsVoiceListRepository(
    private val synthesizer: WindowsSpeechSynthesizer = SapiSpeechSynthesizer(),
) : VoiceListRepository {
    private val mutex = Mutex()
    private var cachedVoices: List<TextToSpeechVoice> = emptyList()

    override suspend fun availableVoices(): List<TextToSpeechVoice> =
        mutex.withLock {
            if (cachedVoices.isEmpty()) {
                cachedVoices = withContext(Dispatchers.IO) { synthesizer.listVoices() }
            }
            cachedVoices
        }
}
