package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.model.SpeechRecognitionMode

/**
 * Repository が保持するモードの変化を、音声の断片を渡す前に [SpeechTranscriber] へ反映する。
 * 変化したときだけ [SpeechTranscriber.setMode] を呼ぶ。
 */
internal class TranscriberModeSync(
    private val transcriber: SpeechTranscriber,
    private val mode: StateFlow<SpeechRecognitionMode>,
) {
    private var applied = SpeechRecognitionMode.WakeWord

    /** 現在のモードを反映し、反映したモードを返す。 */
    fun sync(): SpeechRecognitionMode {
        val current = mode.value
        if (current != applied) {
            transcriber.setMode(current)
            applied = current
        }
        return current
    }
}
