package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository

/**
 * Windowsの音声一覧を排他的に取得し、表示対象の言語（[TTS_CULTURE_NAME]）の音声を含む結果だけを保持する。
 * 含まない場合（失敗・音声未導入・他言語のみ）は、後から音声が導入されても検出できるよう次回の呼び出しで再取得する。
 */
internal class WindowsVoiceListRepository(
    private val synthesizer: WindowsSpeechSynthesizer = SapiSpeechSynthesizer(),
) : VoiceListRepository {
    private val mutex = Mutex()
    private var cachedVoices: List<TextToSpeechVoice> = emptyList()

    override suspend fun availableVoices(): List<TextToSpeechVoice> =
        mutex.withLock {
            if (cachedVoices.isEmpty()) {
                val voices = withContext(Dispatchers.IO) { synthesizer.listVoices() }
                if (voices.any { it.cultureName == TTS_CULTURE_NAME }) cachedVoices = voices
                return@withLock voices
            }
            cachedVoices
        }
}
