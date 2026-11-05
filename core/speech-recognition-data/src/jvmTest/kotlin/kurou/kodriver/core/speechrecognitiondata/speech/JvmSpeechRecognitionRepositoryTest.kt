package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class JvmSpeechRecognitionRepositoryTest {
    private val repository = JvmSpeechRecognitionRepository()

    @Test
    fun `isAvailableはfalseを返す`() {
        assertFalse(repository.isAvailable())
    }

    @Test
    fun `observeRecognitionは利用不可を通知して完了する`() =
        runTest {
            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)),
                repository.observeRecognition().toList(),
            )
        }
}
