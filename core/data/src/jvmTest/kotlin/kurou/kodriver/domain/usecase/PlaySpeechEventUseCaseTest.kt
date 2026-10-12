package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kotlin.test.Test

class PlaySpeechEventUseCaseTest {
    private val engine: TextToSpeechEngine = mockk(relaxed = true)

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の speak が呼ばれる`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(LmuWindowsBlueFlag())

        verify(exactly = 1) { engine.speak(LmuWindowsBlueFlag(), false) }
        confirmVerified(engine)
    }

    @Test
    fun `複数回 invoke を呼ぶと呼んだ順に speak が呼ばれる`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(LmuWindowsYellowFlag())
        useCase(LmuWindowsRedFlag())

        verify(exactly = 1) { engine.speak(LmuWindowsYellowFlag(), false) }
        verify(exactly = 1) { engine.speak(LmuWindowsRedFlag(), false) }
        confirmVerified(engine)
    }

    @Test
    fun `queue true を指定すると TextToSpeechEngine の speak に渡される`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(LmuWindowsCarRight(), queue = true)

        verify(exactly = 1) { engine.speak(LmuWindowsCarRight(), true) }
        confirmVerified(engine)
    }
}
