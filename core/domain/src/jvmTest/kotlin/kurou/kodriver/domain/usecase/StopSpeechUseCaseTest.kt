package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kotlin.test.Test

class StopSpeechUseCaseTest {
    private val engine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の stop が呼ばれる`() {
        val useCase = StopSpeechUseCase(engine)

        useCase()

        verify(exactly = 1) { engine.stop() }
        confirmVerified(engine)
    }
}
