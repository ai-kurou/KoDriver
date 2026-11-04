package kurou.kodriver.data.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

/**
 * デスクトップ版は音声認識を提供しないため、常に利用不可として扱う。
 */
internal class JvmSpeechRecognitionRepository : SpeechRecognitionRepository {
    override fun isAvailable(): Boolean = false

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> =
        flowOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
}
