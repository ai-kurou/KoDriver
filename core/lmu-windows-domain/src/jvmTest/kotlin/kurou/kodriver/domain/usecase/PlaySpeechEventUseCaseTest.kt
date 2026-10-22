package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test

class PlaySpeechEventUseCaseTest {
    private val engine: TextToSpeechEngine = mockk(relaxed = true)
    private val blueFlag = DebugReadoutText(LmuWindowsReadoutItemKey.Flag.BlueFlag, "青旗")
    private val yellowFlag = DebugReadoutText(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag, "黄旗")
    private val redFlag = DebugReadoutText(LmuWindowsReadoutItemKey.Flag.RedFlag, "赤旗")
    private val carRight = DebugReadoutText(LmuWindowsReadoutItemKey.VehicleApproach.Root, "右に車両")

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の speak が呼ばれる`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(blueFlag)

        verify(exactly = 1) { engine.speak(blueFlag, false) }
        confirmVerified(engine)
    }

    @Test
    fun `複数回 invoke を呼ぶと呼んだ順に speak が呼ばれる`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(yellowFlag)
        useCase(redFlag)

        verify(exactly = 1) { engine.speak(yellowFlag, false) }
        verify(exactly = 1) { engine.speak(redFlag, false) }
        confirmVerified(engine)
    }

    @Test
    fun `queue true を指定すると TextToSpeechEngine の speak に渡される`() {
        val useCase = PlaySpeechEventUseCase(engine)

        useCase(carRight, queue = true)

        verify(exactly = 1) { engine.speak(carRight, true) }
        confirmVerified(engine)
    }
}
