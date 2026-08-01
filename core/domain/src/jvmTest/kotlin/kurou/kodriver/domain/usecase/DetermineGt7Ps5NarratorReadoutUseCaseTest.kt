package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetermineGt7Ps5NarratorReadoutUseCaseTest {
    private val useCase = DetermineGt7Ps5NarratorReadoutUseCase()

    @Test
    fun `初回の自己ベスト値では読み上げない`() {
        val decision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(90_000, decision.state.previousBestLapTimeMs)
    }

    @Test
    fun `自己ベストが更新されたら設定された声種別で読み上げる`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5MyBestLapCasual), decision.events)
        assertEquals(89_000, decision.state.personalBestMs)
    }

    @Test
    fun `自己ベストの読み上げが無効なら読み上げない`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(enabledStates = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false)),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(enabledStates = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false)),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(Int.MAX_VALUE, decision.state.personalBestMs)
    }

    @Test
    fun `燃料残り周回数は最速ラップの30秒前を過ぎて閾値以下になったら読み上げる`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 160_000L,
            )

        assertEquals(listOf(SpeechEvent.RemainingFuelLapsWarning(0)), decision.events)
        assertEquals(2, decision.state.lastFuelEvaluationLap)
        assertEquals(0, decision.state.lastAnnouncedRemainingLaps)
    }

    @Test
    fun `燃料残り周回数は読み上げタイミング前なら読み上げない`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(),
                observedAtMs = 159_999L,
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(-1, decision.state.lastFuelEvaluationLap)
    }

    @Test
    fun `燃料残り周回数が無効なら評価済みラップだけ更新して読み上げない`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(remainingFuelLapsEnabled = false),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsEnabled = false),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsEnabled = false),
                observedAtMs = 160_000L,
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastFuelEvaluationLap)
    }

    @Test
    fun `給油後は同じ燃料残り周回数でも再度読み上げる`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val secondLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 30f),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val firstWarningDecision =
            useCase.determineRemainingFuelLaps(
                state = secondLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 30f),
                settings = settings(),
                observedAtMs = 160_000L,
            )
        val refueledDecision =
            useCase.determineRemainingFuelLaps(
                state = firstWarningDecision.state,
                telemetry = telemetry(lapCount = 3, bestLapTimeMs = 90_000, gasLevel = 80f),
                settings = settings(),
                observedAtMs = 200_000L,
            )
        val fourthLapDecision =
            useCase.determineRemainingFuelLaps(
                state = refueledDecision.state,
                telemetry = telemetry(lapCount = 4, bestLapTimeMs = 90_000, gasLevel = 20f),
                settings = settings(),
                observedAtMs = 300_000L,
            )
        val secondWarningDecision =
            useCase.determineRemainingFuelLaps(
                state = fourthLapDecision.state,
                telemetry = telemetry(lapCount = 4, bestLapTimeMs = 90_000, gasLevel = 20f),
                settings = settings(),
                observedAtMs = 360_000L,
            )

        assertEquals(listOf(SpeechEvent.RemainingFuelLapsWarning(0)), firstWarningDecision.events)
        assertTrue(refueledDecision.events.isEmpty())
        assertEquals(-1, refueledDecision.state.lastAnnouncedRemainingLaps)
        assertEquals(50f, refueledDecision.state.fuelTrackingState.totalRefueled)
        assertEquals(listOf(SpeechEvent.RemainingFuelLapsWarning(0)), secondWarningDecision.events)
    }

    @Test
    fun `ラップ数が戻ったら燃料残り周回数の読み上げ履歴をリセットする`() {
        val state =
            Gt7Ps5NarratorState(
                lastAnnouncedRemainingLaps = 2,
                lastFuelEvaluationLap = 5,
                fuelTrackingState =
                    Gt7Ps5FuelTrackingState(
                        raceStartFuel = 100f,
                        raceStartLap = 1,
                        currentLap = 5,
                        currentGasLevel = 20f,
                        bestLapTimeMs = 90_000,
                    ),
            )

        val decision =
            useCase.determineRemainingFuelLaps(
                state = state,
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 200_000L,
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(-1, decision.state.lastAnnouncedRemainingLaps)
        assertEquals(-1, decision.state.lastFuelEvaluationLap)
        assertEquals(1, decision.state.fuelTrackingState.currentLap)
    }

    @Test
    fun `燃料残量が閾値以下になると読み上げる`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 30f, gasCapacity = 100f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残量が閾値ちょうどなら読み上げる`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 15f, gasCapacity = 50f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning), decision.events)
    }

    @Test
    fun `燃料残量の警告状態が継続しても再度読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(remainingFuelWarned = true),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残量が閾値より上に戻ると再度読み上げ可能になる`() {
        val warnedState =
            useCase
                .determineRemainingFuel(
                    state = Gt7Ps5NarratorState(),
                    telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                    settings = settings(remainingFuelThresholdPercentage = 30),
                ).state
        val recoveredState =
            useCase
                .determineRemainingFuel(
                    state = warnedState,
                    telemetry = telemetry(gasLevel = 50f, gasCapacity = 100f),
                    settings = settings(remainingFuelThresholdPercentage = 30),
                ).state
        val rewarnedDecision =
            useCase.determineRemainingFuel(
                state = recoveredState,
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertEquals(false, recoveredState.remainingFuelWarned)
        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning), rewarnedDecision.events)
    }

    @Test
    fun `燃料残量が無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings = settings(remainingFuelEnabled = false),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料容量が0以下なら燃料残量は読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 0f, gasCapacity = 0f),
                settings = settings(),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(false, decision.state.remainingFuelWarned)
    }

    private fun settings(
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true),
        myBestLapVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        remainingFuelLapsThreshold: Int = 3,
        remainingFuelLapsEnabled: Boolean = true,
        remainingFuelThresholdPercentage: Int = 30,
        remainingFuelEnabled: Boolean = true,
    ) = Gt7Ps5NarratorReadoutSettings(
        enabledStates = enabledStates,
        myBestLapVoiceType = myBestLapVoiceType,
        remainingFuelLapsThreshold = remainingFuelLapsThreshold,
        remainingFuelLapsEnabled = remainingFuelLapsEnabled,
        remainingFuelThresholdPercentage = remainingFuelThresholdPercentage,
        remainingFuelEnabled = remainingFuelEnabled,
    )

    private fun telemetry(
        lapCount: Int = 1,
        lapsInRace: Int = 5,
        bestLapTimeMs: Int = 90_000,
        gasLevel: Float = 100f,
        gasCapacity: Float = 100f,
    ) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = lapsInRace,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = gasLevel,
        gasCapacity = gasCapacity,
    )
}
