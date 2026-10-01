package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * OS標準のTTSでテキストを読み上げる。空文字・空白のみのテキストは読み上げない。
 */
class SpeakTextUseCase(
    private val repository: TextToSpeechRepository,
) {
    suspend operator fun invoke(
        text: String,
        queue: Boolean = false,
        volume: Int = 100,
    ) {
        if (text.isBlank()) return
        repository.speak(text, queue, volume)
    }
}
