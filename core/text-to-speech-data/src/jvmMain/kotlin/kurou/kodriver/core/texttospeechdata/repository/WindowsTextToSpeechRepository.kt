package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer
import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * Windows標準の音声合成でテキストを読み上げる [TextToSpeechRepository]。
 *
 * 実際の音声合成の呼び出しは [WindowsSpeechSynthesizer] に切り出しており、ここでは
 * 読み上げ不要なテキストの除外とスレッド（[Dispatchers.IO]）の切り替えのみを担う。
 */
internal class WindowsTextToSpeechRepository(
    private val synthesizer: WindowsSpeechSynthesizer = SapiSpeechSynthesizer(),
) : TextToSpeechRepository {
    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) { synthesizer.isAvailable() }

    override suspend fun speak(
        text: String,
        queue: Boolean,
    ) {
        if (text.isBlank()) return
        withContext(Dispatchers.IO) { synthesizer.speak(text, queue) }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) { synthesizer.stop() }
    }
}
