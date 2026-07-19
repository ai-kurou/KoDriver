package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kotlin.test.Test

class PlaySpeechEventUseCaseTest {

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の speak が呼ばれる`() {
        val engine = mockk<TextToSpeechEngine>(relaxed = true)
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.BlueFlag)

        verify(exactly = 1) { engine.speak(SpeechEvent.BlueFlag, false) }
        confirmVerified(engine)
    }

    @Test
    fun `複数回 invoke を呼ぶと呼んだ順に speak が呼ばれる`() {
        val engine = mockk<TextToSpeechEngine>(relaxed = true)
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.YellowFlag)
        useCase(SpeechEvent.SessionStop)

        verify(exactly = 1) { engine.speak(SpeechEvent.YellowFlag, false) }
        verify(exactly = 1) { engine.speak(SpeechEvent.SessionStop, false) }
        confirmVerified(engine)
    }

    @Test
    fun `queue true を指定すると TextToSpeechEngine の speak に渡される`() {
        val engine = mockk<TextToSpeechEngine>(relaxed = true)
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.CarRight, queue = true)

        verify(exactly = 1) { engine.speak(SpeechEvent.CarRight, true) }
        confirmVerified(engine)
    }
}
