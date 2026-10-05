package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class DetermineGt7Ps5NarratorReadoutUseCaseTest {
    private val useCase = DetermineGt7Ps5NarratorReadoutUseCase()

    @Test
    fun `enabledStatesが空でも例外にならずデフォルトtrueで読み上げる`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(enabledStates = emptyMap()),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(enabledStates = emptyMap()),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5MyBestLap(89_000)), decision.events)
    }

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
    fun `自己ベストが更新されたら更新後のタイムをイベントに保持する`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5MyBestLap(89_000)), decision.events)
        assertEquals(89_000, decision.state.personalBestMs)
    }

    @Test
    fun `ベストラップタイムが0以下なら自己ベストラップを読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 0),
                settings = settings(),
            )

        assertTrue(second.events.isEmpty())
    }

    @Test
    fun `前回のベストラップタイムが0以下でも更新条件を満たせば自己ベストラップを読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 0),
                settings = settings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5MyBestLap(89_000)), second.events)
    }

    @Test
    fun `前回より遅いラップタイムでは読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 91_000),
                settings = settings(),
            )

        assertTrue(second.events.isEmpty())
    }

    @Test
    fun `既に記録している自己ベストより遅ければ読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(),
            )

        val third =
            useCase.determineMyBestLap(
                state = second.state.copy(previousBestLapTimeMs = 95_000),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings = settings(),
            )

        assertTrue(third.events.isEmpty())
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
    fun `自己ベストの読み上げはRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(bestLapTimeMs = 90_000),
                settings =
                    settings(
                        enabledStates =
                            mapOf(
                                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
                                ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled to false,
                            ),
                    ),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings =
                    settings(
                        enabledStates =
                            mapOf(
                                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
                                ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled to false,
                            ),
                    ),
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

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), decision.events)
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
        val disabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false)
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(enabledStates = disabledStates),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(enabledStates = disabledStates),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(enabledStates = disabledStates),
                observedAtMs = 160_000L,
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastFuelEvaluationLap)
    }

    @Test
    fun `燃料残り周回数はRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val disabledStates: Map<ReadoutItemKey, Boolean> =
            mapOf(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled to false,
            )
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(enabledStates = disabledStates),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(enabledStates = disabledStates),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(enabledStates = disabledStates),
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

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
        assertTrue(refueledDecision.events.isEmpty())
        assertEquals(-1, refueledDecision.state.lastAnnouncedRemainingLaps)
        assertEquals(Gt7Ps5FuelUnit(50f), refueledDecision.state.fuelTrackingState.totalRefueled)
        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), secondWarningDecision.events)
    }

    @Test
    fun `同じラップで既に評価済みなら燃料残り周回数を再評価しない`() {
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
        val warningDecision =
            useCase.determineRemainingFuelLaps(
                state = secondLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 30f),
                settings = settings(),
                observedAtMs = 160_000L,
            )
        val sameLapDecision =
            useCase.determineRemainingFuelLaps(
                state = warningDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 29f),
                settings = settings(),
                observedAtMs = 170_000L,
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), warningDecision.events)
        assertTrue(sameLapDecision.events.isEmpty())
        assertEquals(2, sameLapDecision.state.lastFuelEvaluationLap)
        assertEquals(0, sameLapDecision.state.lastAnnouncedRemainingLaps)
    }

    @Test
    fun `タンク容量に対する閾値未満の残量増加は給油として扱わない`() {
        val initialDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, gasLevel = 100f, gasCapacity = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = initialDecision.state,
                telemetry = telemetry(lapCount = 1, gasLevel = 100.3f, gasCapacity = 100f),
                settings = settings(),
                observedAtMs = 1_000L,
            )

        assertEquals(Gt7Ps5FuelUnit(0f), decision.state.fuelTrackingState.totalRefueled)
        assertEquals(false, decision.state.fuelTrackingState.hasRefueled)
        assertEquals(Gt7Ps5FuelUnit(100.3f), decision.state.fuelTrackingState.currentGasLevel)
    }

    @Test
    fun `閾値未満の増加が複数パケットに分かれても累積で閾値に達したら給油として扱う`() {
        var state = Gt7Ps5NarratorState()
        val results =
            listOf(20f, 20.125f, 20.25f, 20.375f, 20.5f).map { gasLevel ->
                state =
                    useCase
                        .determineRemainingFuelLaps(
                            state = state,
                            telemetry = telemetry(lapCount = 1, gasLevel = gasLevel, gasCapacity = 100f),
                            settings = settings(),
                            observedAtMs = 0L,
                        ).state
                state.fuelTrackingState
            }

        assertEquals(listOf(0f, 0f, 0f, 0f, 0.5f), results.map { it.totalRefueled.value })
        assertEquals(true, results.last().hasRefueled)
        assertEquals(Gt7Ps5FuelUnit(20.5f), results.last().refuelBaselineGasLevel)
    }

    @Test
    fun `残量が減ったら給油判定の基準値を追従させる`() {
        var state = Gt7Ps5NarratorState()
        val results =
            listOf(20f, 20.25f, 19.75f, 20f, 20.25f).map { gasLevel ->
                state =
                    useCase
                        .determineRemainingFuelLaps(
                            state = state,
                            telemetry = telemetry(lapCount = 1, gasLevel = gasLevel, gasCapacity = 100f),
                            settings = settings(),
                            observedAtMs = 0L,
                        ).state
                state.fuelTrackingState
            }

        assertEquals(listOf(0f, 0f, 0f, 0f, 0.5f), results.map { it.totalRefueled.value })
        assertEquals(Gt7Ps5FuelUnit(19.75f), results[2].refuelBaselineGasLevel)
    }

    @Test
    fun `タンク容量に対する閾値以上の残量増加は給油として扱う`() {
        val initialDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, gasLevel = 100f, gasCapacity = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = initialDecision.state,
                telemetry = telemetry(lapCount = 1, gasLevel = 100.5f, gasCapacity = 100f),
                settings = settings(),
                observedAtMs = 1_000L,
            )

        assertEquals(Gt7Ps5FuelUnit(0.5f), decision.state.fuelTrackingState.totalRefueled)
        assertEquals(true, decision.state.fuelTrackingState.hasRefueled)
    }

    @Test
    fun `タンク容量が0のEVでは残量が増えても給油として扱わない`() {
        val initialDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, gasLevel = 0f, gasCapacity = 0f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = initialDecision.state,
                telemetry = telemetry(lapCount = 1, gasLevel = 0.3f, gasCapacity = 0f),
                settings = settings(),
                observedAtMs = 1_000L,
            )

        assertEquals(Gt7Ps5FuelUnit(0f), decision.state.fuelTrackingState.totalRefueled)
        assertEquals(false, decision.state.fuelTrackingState.hasRefueled)
    }

    @Test
    fun `閾値未満の残量増加では同じ燃料残り周回数を再度読み上げない`() {
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
        val thirdLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstWarningDecision.state,
                telemetry = telemetry(lapCount = 3, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(),
                observedAtMs = 200_000L,
            )
        val jitterDecision =
            useCase.determineRemainingFuelLaps(
                state = thirdLapDecision.state,
                telemetry = telemetry(lapCount = 3, bestLapTimeMs = 90_000, gasLevel = 10.3f),
                settings = settings(),
                observedAtMs = 260_000L,
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
        assertEquals(Gt7Ps5FuelUnit(0f), jitterDecision.state.fuelTrackingState.totalRefueled)
        assertEquals(false, jitterDecision.state.fuelTrackingState.hasRefueled)
        assertTrue(jitterDecision.events.isEmpty())
        assertEquals(0, jitterDecision.state.lastAnnouncedRemainingLaps)
    }

    @Test
    fun `ラップ数が戻ったら燃料残り周回数の読み上げ履歴をリセットする`() {
        val state =
            Gt7Ps5NarratorState(
                lastAnnouncedRemainingLaps = 2,
                lastFuelEvaluationLap = 5,
                fuelTrackingState =
                    Gt7Ps5FuelTrackingState(
                        raceStartFuel = Gt7Ps5FuelUnit(100f),
                        raceStartLap = 1,
                        currentLap = 5,
                        currentGasLevel = Gt7Ps5FuelUnit(20f),
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
    fun `ベストラップタイムが0以下なら燃料残り周回数を読み上げない`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 0, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 0, gasLevel = 10f),
                settings = settings(),
                observedAtMs = 100_000L,
            )

        assertTrue(decision.events.isEmpty())
    }

    @Test
    fun `完走したラップ数が0以下なら燃料残り周回数を読み上げない`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 0L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 90f),
                settings = settings(),
                observedAtMs = 160_000L,
            )

        assertTrue(decision.events.isEmpty())
    }

    @Test
    fun `燃料消費量が0以下なら燃料残り周回数を読み上げない`() {
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
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(),
                observedAtMs = 160_000L,
            )

        assertTrue(decision.events.isEmpty())
    }

    @Test
    fun `残り周回数が閾値を超える場合は読み上げない`() {
        val firstLapDecision =
            useCase.determineRemainingFuelLaps(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(lapCount = 1, bestLapTimeMs = 90_000, gasLevel = 100f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 99f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determineRemainingFuelLaps(
                state = nextLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 99f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 160_000L,
            )

        assertTrue(decision.events.isEmpty())
    }

    @Test
    fun `同じ残り周回数の評価が続く間は再度読み上げない`() {
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
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 100_000L,
            )
        val firstWarningDecision =
            useCase.determineRemainingFuelLaps(
                state = secondLapDecision.state,
                telemetry = telemetry(lapCount = 2, bestLapTimeMs = 90_000, gasLevel = 10f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 160_000L,
            )
        val thirdLapDecision =
            useCase.determineRemainingFuelLaps(
                state = firstWarningDecision.state,
                telemetry = telemetry(lapCount = 3, bestLapTimeMs = 90_000, gasLevel = 9f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 200_000L,
            )
        val secondEvaluationDecision =
            useCase.determineRemainingFuelLaps(
                state = thirdLapDecision.state,
                telemetry = telemetry(lapCount = 3, bestLapTimeMs = 90_000, gasLevel = 9f),
                settings = settings(remainingFuelLapsThreshold = 3),
                observedAtMs = 260_000L,
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
        assertTrue(secondEvaluationDecision.events.isEmpty())
    }

    @Test
    fun `燃料残量が閾値以下になると読み上げる`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 30f, gasCapacity = 100f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning(30)), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残量は閾値ではなく実際の割合を四捨五入し0から100に収める`() {
        listOf(29.6f to 30, 29.4f to 29, 0.1f to 0, 100f to 100, 110f to 100).forEach { (level, percent) ->
            val decision =
                useCase.determineRemainingFuel(
                    state = Gt7Ps5NarratorState(),
                    telemetry = telemetry(gasLevel = level, gasCapacity = 100f),
                    settings = settings(remainingFuelThresholdPercentage = level.toInt().coerceAtLeast(30)),
                )

            assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning(percent)), decision.events)
            assertEquals(true, decision.state.remainingFuelWarned)
        }
    }

    @Test
    fun `燃料残量が閾値ちょうどなら読み上げる`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 15f, gasCapacity = 50f),
                settings = settings(remainingFuelThresholdPercentage = 30),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning(30)), decision.events)
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
        assertEquals(listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning(20)), rewarnedDecision.events)
    }

    @Test
    fun `燃料残量が無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings = settings(enabledStates = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to false)),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残量はRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings =
                    settings(
                        enabledStates =
                            mapOf(
                                ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                                ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled to false,
                            ),
                    ),
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

    @Test
    fun `燃料残量が正でも容量が0以下なら燃料残量は読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 0f),
                settings = settings(),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(false, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残量と容量のどちらかが非有限なら燃料残量は読み上げない`() {
        listOf(
            Float.POSITIVE_INFINITY to Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY to 100f,
            20f to Float.POSITIVE_INFINITY,
            Float.NaN to 100f,
            20f to Float.NaN,
        ).forEach { (gasLevel, gasCapacity) ->
            val decision =
                useCase.determineRemainingFuel(
                    state = Gt7Ps5NarratorState(),
                    telemetry = telemetry(gasLevel = gasLevel, gasCapacity = gasCapacity),
                    settings = settings(),
                )

            assertTrue(decision.events.isEmpty())
            assertEquals(false, decision.state.remainingFuelWarned)
        }
    }

    @Test
    fun `タイヤ温度が高温閾値以上になると読み上げる`() {
        val decision =
            useCase.determineTyreTemperature(
                state = Gt7Ps5NarratorState(),
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(95f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
            )

        assertEquals(listOf(SpeechEvent.Gt7Ps5TyreOverheat(95)), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `過熱状態が継続しても再度読み上げない`() {
        val decision =
            useCase.determineTyreTemperature(
                state = Gt7Ps5NarratorState(tyreOverheating = true),
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(95f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `ヒステリシス範囲内に留まる間は過熱状態を維持する`() {
        val overheatedState =
            useCase
                .determineTyreTemperature(
                    state = Gt7Ps5NarratorState(),
                    telemetry =
                        telemetry(
                            tyreTemperature =
                                Gt7Ps5TyreTemperatureData(
                                    CelsiusReading(95f),
                                    CelsiusReading(90f),
                                    CelsiusReading(90f),
                                    CelsiusReading(90f),
                                ),
                        ),
                    settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
                ).state
        val decision =
            useCase.determineTyreTemperature(
                state = overheatedState,
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(92f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `ヒステリシスを下回るまで冷えると再度読み上げ可能になる`() {
        val overheatedState =
            useCase
                .determineTyreTemperature(
                    state = Gt7Ps5NarratorState(),
                    telemetry =
                        telemetry(
                            tyreTemperature =
                                Gt7Ps5TyreTemperatureData(
                                    CelsiusReading(95f),
                                    CelsiusReading(90f),
                                    CelsiusReading(90f),
                                    CelsiusReading(90f),
                                ),
                        ),
                    settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
                ).state
        val cooledState =
            useCase
                .determineTyreTemperature(
                    state = overheatedState,
                    telemetry =
                        telemetry(
                            tyreTemperature =
                                Gt7Ps5TyreTemperatureData(
                                    CelsiusReading(85f),
                                    CelsiusReading(85f),
                                    CelsiusReading(85f),
                                    CelsiusReading(85f),
                                ),
                        ),
                    settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
                ).state
        val decision =
            useCase.determineTyreTemperature(
                state = cooledState,
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(95f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
            )

        assertEquals(false, cooledState.tyreOverheating)
        assertEquals(listOf(SpeechEvent.Gt7Ps5TyreOverheat(95)), decision.events)
    }

    @Test
    fun `ヒステリシス範囲内の初期温度では過熱状態にならない`() {
        val decision =
            useCase.determineTyreTemperature(
                state = Gt7Ps5NarratorState(),
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(92f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(false, decision.state.tyreOverheating)
    }

    @Test
    fun `タイヤ温度の読み上げが無効なら読み上げない`() {
        val decision =
            useCase.determineTyreTemperature(
                state = Gt7Ps5NarratorState(),
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(95f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings =
                    settings(
                        enabledStates = mapOf(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to false),
                        tyreTemperatureHighThresholdCelsius = Celsius(95),
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `過熱警告の読み上げが無効なら読み上げない`() {
        val decision =
            useCase.determineTyreTemperature(
                state = Gt7Ps5NarratorState(),
                telemetry =
                    telemetry(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(95f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                                CelsiusReading(90f),
                            ),
                    ),
                settings =
                    settings(
                        enabledStates = mapOf(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning to false),
                        tyreTemperatureHighThresholdCelsius = Celsius(95),
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.tyreOverheating)
    }

    private fun settings(
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true),
        remainingFuelLapsThreshold: Int = 3,
        remainingFuelThresholdPercentage: Int = 30,
        tyreTemperatureHighThresholdCelsius: Celsius = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
    ) = Gt7Ps5NarratorReadoutSettings(
        enabledStates = enabledStates,
        remainingFuelLapsThreshold = remainingFuelLapsThreshold,
        remainingFuelThresholdPercentage = remainingFuelThresholdPercentage,
        tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThresholdCelsius,
    )

    private fun telemetry(
        lapCount: Int = 1,
        lapsInRace: Int = 5,
        bestLapTimeMs: Int = 90_000,
        gasLevel: Float = 100f,
        gasCapacity: Float = 100f,
        tyreTemperature: Gt7Ps5TyreTemperatureData =
            Gt7Ps5TyreTemperatureData(CelsiusReading(0f), CelsiusReading(0f), CelsiusReading(0f), CelsiusReading(0f)),
    ) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = lapsInRace,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = Gt7Ps5FuelUnit(gasLevel),
        gasCapacity = Gt7Ps5FuelUnit(gasCapacity),
        tyreTemperature = tyreTemperature,
    )

    @Test
    fun `全輪の最高温度を整数に丸めて過熱イベントに渡す`() {
        for ((temperature, rounded) in listOf(95.4f to 95, 107.5f to 108)) {
            for (hotWheel in 0..3) {
                val wheels = List(4) { CelsiusReading(if (it == hotWheel) temperature else 0f) }
                val decision =
                    useCase.determineTyreTemperature(
                        state = Gt7Ps5NarratorState(),
                        telemetry =
                            telemetry(
                                tyreTemperature = Gt7Ps5TyreTemperatureData(wheels[0], wheels[1], wheels[2], wheels[3]),
                            ),
                        settings = settings(),
                    )
                assertEquals(listOf(SpeechEvent.Gt7Ps5TyreOverheat(rounded)), decision.events)
            }
        }
    }
}
