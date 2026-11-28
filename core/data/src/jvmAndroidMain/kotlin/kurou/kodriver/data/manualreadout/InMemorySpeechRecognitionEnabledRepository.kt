package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository

/**
 * 音声認識の ON/OFF をメモリに持つ。永続化はしない。
 */
internal class InMemorySpeechRecognitionEnabledRepository : SpeechRecognitionEnabledRepository {
    private val _enabled = MutableStateFlow(false)
    override val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    override fun update(enabled: Boolean) {
        _enabled.value = enabled
    }
}
