@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextToSpeechEngine : TextToSpeechEngine {
    val spokenEvents = mutableListOf<SpeechEvent>()
    val queued = mutableListOf<Boolean>()
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) {
        spokenEvents.add(event)
        queued.add(queue)
    }
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

    @Test
    fun `queue true を指定すると TextToSpeechEngine の speak に渡される`() {
        val engine = FakeTextToSpeechEngine()
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(SpeechEvent.CarRight, queue = true)

        assertEquals(listOf(true), engine.queued)
    }
}
