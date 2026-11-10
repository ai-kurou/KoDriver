package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.TextToSpeechRepository

/**
 * OS標準のTTSでテキストを読み上げる。空文字・空白のみのテキストは読み上げない。
 * 声の高さは保存済み設定を使う。
 * 読み上げ速度は保存済み設定を使う。
 * [invoke] の音声IDが未指定なら保存済み設定を使い、指定時はその音声で読み上げる。
 * [invoke] の音声用途が未指定ならRepositoryの保存済み設定を使う。
 * TTSが「℃」を「せっし」と読む（100℃が「ひゃくせっし」になる）ため、
 * Repositoryへ渡す前に「℃」を「ど」へ置き換えて必ず「ど」と読ませる。
 */
class SpeakTextUseCase(
    private val repository: TextToSpeechRepository,
    private val observeVoice: ObserveVoiceUseCase,
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase,
    private val observeVoicePitch: ObserveVoicePitchUseCase,
) {
    suspend operator fun invoke(
        text: String,
        queue: Boolean = false,
        volume: Int = 100,
        voiceId: String? = null,
        audioUsage: AndroidAudioUsage? = null,
    ) {
        if (text.isBlank()) return
        val speed = observeVoiceSpeed().first()
        val pitch = observeVoicePitch().first()
        val readableText = text.replace(CELSIUS_SIGN, CELSIUS_READING)
        repository.speak(readableText, queue, volume, voiceId ?: observeVoice().first(), speed, pitch, audioUsage)
    }
}

private const val CELSIUS_SIGN = "\u2103"
private const val CELSIUS_READING = "ど"
