package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
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

    @Test
    fun `初回観測時は読み上げない`() {
        val decision = useCase.determineFlag(
            state = AceWindowsNarratorState(),
            data = flag(AceWindowsFlagType.BLUE_FLAG),
            settings = flagSettings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `フラグが変化すると対応するイベントを読み上げる`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.BLUE_FLAG),
            settings = flagSettings(),
        )

        assertEquals(listOf(SpeechEvent.AceWindowsBlueFlag), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `フラグが変化しなければ読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.BLUE_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.BLUE_FLAG),
            settings = flagSettings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `フラグ項目全体が無効なら読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.BLUE_FLAG),
            settings = flagSettings(enabledOverrides = mapOf(ReadoutItemKey.AceWindows.Flag.Root to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `個別のフラグ項目が無効なら読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.BLUE_FLAG),
            settings = flagSettings(enabledOverrides = mapOf(ReadoutItemKey.AceWindows.Flag.BlueFlag to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `NO_FLAGへの変化は読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.BLUE_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.NO_FLAG),
            settings = flagSettings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.NO_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `UNKNOWNへの変化は読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.BLUE_FLAG)

        val decision = useCase.determineFlag(
            state = state,
            data = flag(AceWindowsFlagType.UNKNOWN),
            settings = flagSettings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.UNKNOWN, decision.state.previousFlag)
    }

    @Test
    fun `各フラグ種別に対応するイベントを読み上げる`() {
        val expected = mapOf(
            AceWindowsFlagType.WHITE_FLAG to SpeechEvent.AceWindowsWhiteFlag,
            AceWindowsFlagType.GREEN_FLAG to SpeechEvent.AceWindowsGreenFlag,
            AceWindowsFlagType.RED_FLAG to SpeechEvent.AceWindowsRedFlag,
            AceWindowsFlagType.BLUE_FLAG to SpeechEvent.AceWindowsBlueFlag,
            AceWindowsFlagType.YELLOW_FLAG to SpeechEvent.AceWindowsYellowFlag,
            AceWindowsFlagType.BLACK_FLAG to SpeechEvent.AceWindowsBlackFlag,
            AceWindowsFlagType.BLACK_WHITE_FLAG to SpeechEvent.AceWindowsBlackWhiteFlag,
            AceWindowsFlagType.CHECKERED_FLAG to SpeechEvent.AceWindowsCheckeredFlag,
            AceWindowsFlagType.ORANGE_CIRCLE_FLAG to SpeechEvent.AceWindowsOrangeCircleFlag,
            AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG to SpeechEvent.AceWindowsRedYellowStripesFlag,
        )

        expected.forEach { (flagType, event) ->
            val decision = useCase.determineFlag(
                state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG),
                data = flag(flagType),
                settings = flagSettings(),
            )
            assertEquals(listOf(event), decision.events)
        }
    }

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = remainingPercent)

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun settings(
        thresholdPercentage: Int,
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.AceWindows.RemainingFuel.Root to true),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates = enabledStates,
        remainingFuelThresholdPercentage = thresholdPercentage,
    )

    private fun flagSettings(
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates = mapOf(
            ReadoutItemKey.AceWindows.Flag.Root to true,
            ReadoutItemKey.AceWindows.Flag.WhiteFlag to true,
            ReadoutItemKey.AceWindows.Flag.GreenFlag to true,
            ReadoutItemKey.AceWindows.Flag.RedFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlueFlag to true,
            ReadoutItemKey.AceWindows.Flag.YellowFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlackFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to true,
            ReadoutItemKey.AceWindows.Flag.CheckeredFlag to true,
            ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to true,
            ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to true,
        ) + enabledOverrides,
        remainingFuelThresholdPercentage = 0,
    )
}
