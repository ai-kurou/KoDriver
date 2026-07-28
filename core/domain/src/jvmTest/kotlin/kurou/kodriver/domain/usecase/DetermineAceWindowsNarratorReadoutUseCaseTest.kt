package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class DetermineAceWindowsNarratorReadoutUseCaseTest {

    private val useCase = DetermineAceWindowsNarratorReadoutUseCase()

    @Test
    fun `残量が閾値以下になると読み上げる`() {
        val decision = useCase.determineRemainingFuel(
            state = AceWindowsNarratorState(),
            data = fuel(remainingPercent = 20.0),
            settings = settings(thresholdPercentage = 30),
        )

        assertEquals(listOf(SpeechEvent.AceWindowsRemainingFuelWarning), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `閾値ちょうどは低燃料扱い`() {
        val decision = useCase.determineRemainingFuel(
            state = AceWindowsNarratorState(),
            data = fuel(remainingPercent = 30.0),
            settings = settings(thresholdPercentage = 30),
        )

        assertEquals(listOf(SpeechEvent.AceWindowsRemainingFuelWarning), decision.events)
    }

    @Test
    fun `警告状態が継続しても再度読み上げない`() {
        val state = AceWindowsNarratorState(remainingFuelWarned = true)

        val decision = useCase.determineRemainingFuel(
            state = state,
            data = fuel(remainingPercent = 20.0),
            settings = settings(thresholdPercentage = 30),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `残量が閾値より上に戻ると再度読み上げ可能になる`() {
        val warnedState = useCase.determineRemainingFuel(
            state = AceWindowsNarratorState(),
            data = fuel(remainingPercent = 20.0),
            settings = settings(thresholdPercentage = 30),
        ).state

        val recoveredState = useCase.determineRemainingFuel(
            state = warnedState,
            data = fuel(remainingPercent = 50.0),
            settings = settings(thresholdPercentage = 30),
        ).state

        val rewarnedDecision = useCase.determineRemainingFuel(
            state = recoveredState,
            data = fuel(remainingPercent = 20.0),
            settings = settings(thresholdPercentage = 30),
        )

        assertEquals(false, recoveredState.remainingFuelWarned)
        assertEquals(listOf(SpeechEvent.AceWindowsRemainingFuelWarning), rewarnedDecision.events)
    }

    @Test
    fun `残り燃料項目が無効なら読み上げない`() {
        val decision = useCase.determineRemainingFuel(
            state = AceWindowsNarratorState(),
            data = fuel(remainingPercent = 20.0),
            settings = settings(
                thresholdPercentage = 30,
                enabledStates = mapOf(ReadoutItemKey.AceWindows.RemainingFuel.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = remainingPercent)

    private fun settings(
        thresholdPercentage: Int,
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.AceWindows.RemainingFuel.Root to true),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates = enabledStates,
        remainingFuelThresholdPercentage = thresholdPercentage,
    )
}
