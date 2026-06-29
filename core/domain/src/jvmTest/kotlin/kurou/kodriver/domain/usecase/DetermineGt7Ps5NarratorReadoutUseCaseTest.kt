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
        val decision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(bestLapTimeMs = 90_000),
            settings = settings(),
            observedAtMs = 0L,
        )

        assertTrue(decision.events.isEmpty())
        assertEquals(90_000, decision.state.previousBestLapTimeMs)
    }

    @Test
    fun `自己ベストが更新されたら設定された声種別で読み上げる`() {
        val initialDecision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(bestLapTimeMs = 90_000),
            settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
            observedAtMs = 0L,
        )
        val decision = useCase(
            state = initialDecision.state,
            telemetry = telemetry(bestLapTimeMs = 89_000),
            settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
            observedAtMs = 1_000L,
        )

        assertEquals(listOf(SpeechEvent.MyBestLapCasual), decision.events)
        assertEquals(89_000, decision.state.personalBestMs)
    }

    @Test
    fun `自己ベストの読み上げが無効なら読み上げない`() {
        val initialDecision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(bestLapTimeMs = 90_000),
            settings = settings(enabledStates = mapOf(ReadoutItemKey.MyBestLap to false)),
            observedAtMs = 0L,
        )
        val decision = useCase(
            state = initialDecision.state,
            telemetry = telemetry(bestLapTimeMs = 89_000),
            settings = settings(enabledStates = mapOf(ReadoutItemKey.MyBestLap to false)),
            observedAtMs = 1_000L,
        )

        assertTrue(decision.events.isEmpty())
        assertEquals(Int.MAX_VALUE, decision.state.personalBestMs)
    }

    @Test
    fun `燃料残り周回数は最速ラップの30秒前を過ぎて閾値以下になったら読み上げる`() {
        val firstLapDecision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
            settings = settings(),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase(
            state = firstLapDecision.state,
            telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
            settings = settings(remainingFuelLapsThreshold = 3),
            observedAtMs = 100_000L,
        )
        val decision = useCase(
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
        val firstLapDecision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
            settings = settings(),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase(
            state = firstLapDecision.state,
            telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
            settings = settings(),
            observedAtMs = 100_000L,
        )
        val decision = useCase(
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
        val firstLapDecision = useCase(
            state = Gt7Ps5NarratorState(),
            telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
            settings = settings(remainingFuelLapsEnabled = false),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase(
            state = firstLapDecision.state,
            telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
            settings = settings(remainingFuelLapsEnabled = false),
            observedAtMs = 100_000L,
        )
        val decision = useCase(
            state = nextLapDecision.state,
            telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
            settings = settings(remainingFuelLapsEnabled = false),
            observedAtMs = 160_000L,
        )

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastFuelEvaluationLap)
    }

    @Test
    fun `ラップ数が戻ったら燃料残り周回数の読み上げ履歴をリセットする`() {
        val state = Gt7Ps5NarratorState(
            lastAnnouncedRemainingLaps = 2,
            lastFuelEvaluationLap = 5,
            fuelTrackingState = Gt7Ps5FuelTrackingState(
                raceStartFuel = 100f,
                raceStartLap = 1,
                currentLap = 5,
                currentGasLevel = 20f,
                bestLapTimeMs = 90_000,
            ),
        )

        val decision = useCase(
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

    private fun settings(
        enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
        myBestLapVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        remainingFuelLapsThreshold: Int = 3,
        remainingFuelLapsEnabled: Boolean = true,
    ) = Gt7Ps5NarratorReadoutSettings(
        enabledStates = enabledStates,
        myBestLapVoiceType = myBestLapVoiceType,
        remainingFuelLapsThreshold = remainingFuelLapsThreshold,
        remainingFuelLapsEnabled = remainingFuelLapsEnabled,
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
