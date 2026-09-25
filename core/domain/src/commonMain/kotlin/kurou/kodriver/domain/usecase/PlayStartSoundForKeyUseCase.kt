package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * [key] に紐づく開始音を再生し、再生完了まで待つ。OS標準TTSでの読み上げの前に、
 * 収録音声と同じ開始音を鳴らしたい場合に使う。
 */
class PlayStartSoundForKeyUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    suspend operator fun invoke(key: ReadoutItemKey) = ttsEngine.playStartSound(key)
}
