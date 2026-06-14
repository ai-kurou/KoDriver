@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextToSpeechEngine : TextToSpeechEngine {
    val spokenEvents = mutableListOf<SpeechEvent>()
    override val currentReadoutItemKey: String? = null
    override fun speak(event: SpeechEvent) { spokenEvents.add(event) }
    override fun stop() = Unit
}

class PlaySpeechEventUseCaseTest {

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の speak が呼ばれる`() {
        val engine = FakeTextToSpeechEngine()
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.BlueFlag)

        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), engine.spokenEvents)
    }

    @Test
    fun `複数回 invoke を呼ぶと呼んだ順に speak が呼ばれる`() {
        val engine = FakeTextToSpeechEngine()
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.YellowFlag)
        useCase(SpeechEvent.SessionStop)

        assertEquals(listOf(SpeechEvent.YellowFlag, SpeechEvent.SessionStop), engine.spokenEvents)
    }
}
