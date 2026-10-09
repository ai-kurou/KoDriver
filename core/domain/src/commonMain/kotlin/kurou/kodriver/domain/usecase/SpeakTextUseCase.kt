package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * OS標準のTTSでテキストを読み上げる。空文字・空白のみのテキストは読み上げない。
 * 読み上げ速度は保存済み設定を使う。
 * [invoke] の音声IDが未指定なら保存済み設定を使い、指定時はその音声で読み上げる。
 */
class SpeakTextUseCase(
    private val repository: TextToSpeechRepository,
    private val observeVoice: ObserveVoiceUseCase,
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase,
) {
    suspend operator fun invoke(
        text: String,
        queue: Boolean = false,
        volume: Int = 100,
        voiceId: String? = null,
    ) {
        if (text.isBlank()) return
        val speed = observeVoiceSpeed().first()
        repository.speak(text, queue, volume, voiceId ?: observeVoice().first(), speed)
    }
}
