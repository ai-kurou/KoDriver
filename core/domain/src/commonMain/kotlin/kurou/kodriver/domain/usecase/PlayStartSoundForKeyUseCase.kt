package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * [key] に紐づく開始音を再生し、再生完了まで待つ。OS標準TTSでの読み上げの前に、
 * 収録音声と同じ開始音を鳴らしたい場合に使う。
 */
public class PlayStartSoundForKeyUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    public suspend operator fun invoke(key: ReadoutItemKey): Unit = ttsEngine.playStartSound(key)
}
