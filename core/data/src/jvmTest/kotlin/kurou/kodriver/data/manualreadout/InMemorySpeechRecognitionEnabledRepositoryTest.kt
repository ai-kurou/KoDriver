package kurou.kodriver.data.manualreadout

import kotlin.test.Test
import kotlin.test.assertEquals

class InMemorySpeechRecognitionEnabledRepositoryTest {
    private val repository = InMemorySpeechRecognitionEnabledRepository()

    @Test
    fun `初期状態はOFF`() {
        assertEquals(false, repository.enabled.value)
    }

    @Test
    fun `updateで状態を置き換える`() {
        repository.update(true)
        assertEquals(true, repository.enabled.value)

        repository.update(false)
        assertEquals(false, repository.enabled.value)
    }
}
