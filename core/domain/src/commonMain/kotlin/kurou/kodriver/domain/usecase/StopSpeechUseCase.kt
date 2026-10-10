package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey

class StopSpeechUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    /** 現在再生中のイベントが [key] に属する場合だけ停止する。再生が終わっている場合や別項目の読み上げ中は止めない。 */
    operator fun invoke(key: ReadoutItemKey) {
        if (ttsEngine.currentReadoutItemKey == key) ttsEngine.stop()
    }
}
