package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.Gt7Ps5ManualReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5ManualReadoutItem
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class DetermineGt7Ps5NarratorReadoutUseCaseTest {
    private val useCase = DetermineGt7Ps5NarratorReadoutUseCase()

    @Test
    fun `手動周回数は現在値とゼロ以下とデータなしと空欄を区別する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value
        val cases =
            listOf(
                Gt7Ps5ManualReadoutItem(key, "あと{laps}周", 3) to "あと3周",
                Gt7Ps5ManualReadoutItem(key, "あと{laps}周", 0) to "燃料残り1周未満",
                Gt7Ps5ManualReadoutItem(key, "あと{laps}周", -1) to "燃料残り1周未満",
                Gt7Ps5ManualReadoutItem(key, "あと{laps}周", null) to "残り燃料の周回数はデータがありません",
                Gt7Ps5ManualReadoutItem(key, " ", null) to "",
                Gt7Ps5ManualReadoutItem(key, "", 0) to "",
            )
        cases.forEach { (item, expected) ->
            val state = Gt7Ps5NarratorState(lastAnnouncedRemainingLaps = 3)
            val decision = useCase.determineManualReadout(state, listOf(item))
            assertTrue(state === decision.state)
            assertEquals(expected, decision.events.single().narratedText)
        }
        assertEquals(
            "燃料は残り42パーセント、あと3周",
            useCase
                .determineManualReadout(
                    Gt7Ps5NarratorState(),
                    listOf(
                        Gt7Ps5ManualReadoutItem(
                            Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                            "燃料は残り{percent}パーセント",
                            42,
                        ),
                        Gt7Ps5ManualReadoutItem(key, "あと{laps}周", 3),
                    ),
                ).events
                .single()
                .narratedText,
        )
    }

    @Test
    fun `現在周回数は平均消費と給油を考慮し算出できない場合はnullにする`() {
        val fuel =
            Gt7Ps5FuelTrackingState(
                raceStartFuel = Gt7Ps5FuelUnit(100f),
                raceStartLap = 1,
                currentLap = 3,
                currentGasLevel = Gt7Ps5FuelUnit(50f),
            )
        val cases =
            listOf(
                fuel to 2,
                fuel.copy(currentGasLevel = Gt7Ps5FuelUnit(90f)) to 26,
                fuel.copy(totalRefueled = Gt7Ps5FuelUnit(50f)) to 1,
                fuel.copy(currentGasLevel = Gt7Ps5FuelUnit(0f)) to 0,
                fuel.copy(currentGasLevel = Gt7Ps5FuelUnit(1f)) to 0,
                fuel.copy(raceStartFuel = null) to null,
                fuel.copy(raceStartLap = null) to null,
                fuel.copy(currentLap = 1) to null,
                fuel.copy(currentLap = 0) to null,
                fuel.copy(currentGasLevel = Gt7Ps5FuelUnit(100f)) to null,
                fuel.copy(currentGasLevel = Gt7Ps5FuelUnit(101f)) to null,
            )
        cases.forEach { (tracking, expected) ->
            assertEquals(
                expected,
                useCase.currentRemainingFuelLaps(
                    Gt7Ps5NarratorState(
                        fuelTrackingState = tracking,
                        lastFuelEvaluationLap = 3,
                        lastAnnouncedRemainingLaps = 2,
                    ),
                ),
            )
        }
    }

    @Test
    fun `手動燃料は編集文言とデータなしを保存順に連結し空欄と他シムのキーを除外する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        val state = Gt7Ps5NarratorState(remainingFuelWarned = true)
        val decision =
            useCase.determineManualReadout(
                state,
                listOf(
                    Gt7Ps5ManualReadoutItem("unknown", "読まない", 50),
                    Gt7Ps5ManualReadoutItem(LmuWindowsReadoutItemKey.TyreWear.Root.value, "読まない", 50),
                    Gt7Ps5ManualReadoutItem(key, "あと{percent}% {unknown}", 42),
                    Gt7Ps5ManualReadoutItem(key, "", 20),
                    Gt7Ps5ManualReadoutItem(key, manualReadoutItemTextDefault(key), null),
                    Gt7Ps5ManualReadoutItem(key, "上限外", 10),
                ),
            )
        assertTrue(state === decision.state)
        assertEquals(
            listOf(Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, "あと42% {unknown}、残り燃料はデータがありません")),
            decision.events,
        )
    }

    @Test
    fun `手動燃料は既定文言と全データなしと空白と対応項目なしを区別する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        val cases =
            listOf(
                listOf(Gt7Ps5ManualReadoutItem(key, manualReadoutItemTextDefault(key), 0)) to "燃料は残り0パーセント",
                listOf(Gt7Ps5ManualReadoutItem(key, "任意", null)) to "残り燃料はデータがありません",
                listOf(Gt7Ps5ManualReadoutItem(key, "  ", 100)) to "",
                listOf(Gt7Ps5ManualReadoutItem(key, "", null)) to "",
                listOf(Gt7Ps5ManualReadoutItem("unknown", "任意", 50)) to "読み上げる項目がありません",
            )
        cases.forEach { (items, text) ->
            assertEquals(
                listOf(Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, text)),
                useCase.determineManualReadout(Gt7Ps5NarratorState(), items).events,
            )
        }
    }

    @Test
    fun `手動燃料の現在値は整数に丸め容量で割り未受信と不正値はデータなしにする`() {
        assertEquals(null, useCase.currentRemainingFuelPercent(null))
        val cases =
            listOf(
                Triple(42.5f, 100f, 43),
                Triple(21f, 50f, 42),
                Triple(0f, 100f, 0),
                Triple(120f, 100f, 100),
                Triple(Float.NaN, 100f, null),
                Triple(Float.POSITIVE_INFINITY, 100f, null),
                Triple(10f, Float.NaN, null),
                Triple(10f, Float.POSITIVE_INFINITY, null),
                Triple(-1f, 100f, null),
                Triple(10f, 0f, null),
                Triple(10f, -1f, null),
            )
        cases.forEach { (level, capacity, expected) ->
            assertEquals(
                expected,
                useCase.currentRemainingFuelPercent(telemetry(gasLevel = level, gasCapacity = capacity)),
            )
        }
    }

    @Test
    fun `手動読み上げは固定の項目なし文言を返し自動判定の状態を変更しない`() {
        val state = Gt7Ps5NarratorState(personalBestMs = 80_000, previousLapCount = 5)
        val decision = useCase.determineManualReadout(state)
        assertEquals(state, decision.state)
        assertTrue(state === decision.state)
        assertEquals(
            listOf(Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")),
            decision.events,
        )
    }

    @Test
    fun `周回数が減少したら自己ベストをリセットして新セッションの更新を読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state =
                    Gt7Ps5NarratorState(
                        personalBestMs = 89_000,
                        previousBestLapTimeMs = 89_000,
                        previousLapCount = 5,
                    ),
                telemetry = telemetry(bestLapTimeMs = 99_000, lapCount = 1),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
        assertEquals(Int.MAX_VALUE, first.state.personalBestMs)
        assertEquals(99_000, first.state.previousBestLapTimeMs)
        assertEquals(1, first.state.previousLapCount)

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 98_000, lapCount = 2),
                settings = settings(),
            )

        assertEquals(listOf(Gt7Ps5MyBestLap(98_000)), second.events)
        assertEquals(98_000, second.state.personalBestMs)
        assertEquals(2, second.state.previousLapCount)
    }

    @Test
    fun `周回数が同じか増加した場合は自己ベストをリセットしない`() {
        for (currentLap in listOf(5, 6)) {
            val decision =
                useCase.determineMyBestLap(
                    state =
                        Gt7Ps5NarratorState(
                            personalBestMs = 89_000,
                            previousBestLapTimeMs = 99_000,
                            previousLapCount = 5,
                        ),
                    telemetry = telemetry(bestLapTimeMs = 98_000, lapCount = currentLap),
                    settings = settings(),
                )

            assertEquals(emptyList<SpeechEvent>(), decision.events)
            assertEquals(89_000, decision.state.personalBestMs)
            assertEquals(98_000, decision.state.previousBestLapTimeMs)
            assertEquals(currentLap, decision.state.previousLapCount)
        }
    }

    @Test
    fun `自己ベスト判定の各経路で現在の周回数を記録する`() {
        val initialState = Gt7Ps5NarratorState(previousBestLapTimeMs = 90_000, previousLapCount = 1)
        val cases =
            listOf<Triple<Gt7Ps5NarratorState, Int, Map<ReadoutItemKey, Boolean>>>(
                Triple(Gt7Ps5NarratorState(), 89_000, emptyMap()),
                Triple(initialState, 0, emptyMap()),
                Triple(initialState, 90_000, emptyMap()),
                Triple(initialState.copy(personalBestMs = 88_000), 89_000, emptyMap()),
                Triple(initialState, 89_000, mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root to false)),
                Triple(initialState, 89_000, mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled to false)),
                Triple(initialState, 89_000, emptyMap()),
            )
        for ((state, current, enabledStates) in cases) {
            val decision =
                useCase.determineMyBestLap(
                    state = state,
                    telemetry = telemetry(bestLapTimeMs = current, lapCount = 2),
                    settings = settings(enabledStates = enabledStates),
                )

            assertEquals(current, decision.state.previousBestLapTimeMs)
            assertEquals(2, decision.state.previousLapCount)
        }
    }

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

        assertEquals(listOf(Gt7Ps5MyBestLap(89_000)), decision.events)
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

        assertEquals(listOf(Gt7Ps5MyBestLap(89_000)), decision.events)
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

        assertEquals(listOf(Gt7Ps5MyBestLap(89_000)), second.events)
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
                settings = settings(enabledStates = mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root to false)),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                telemetry = telemetry(bestLapTimeMs = 89_000),
                settings = settings(enabledStates = mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root to false)),
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
                                Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
                                Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled to false,
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
                                Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
                                Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled to false,
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

        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), decision.events)
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
        val disabledStates: Map<ReadoutItemKey, Boolean> = mapOf(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to false)
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
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled to false,
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

        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
        assertTrue(refueledDecision.events.isEmpty())
        assertEquals(-1, refueledDecision.state.lastAnnouncedRemainingLaps)
        assertEquals(Gt7Ps5FuelUnit(50f), refueledDecision.state.fuelTrackingState.totalRefueled)
        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), secondWarningDecision.events)
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

        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), warningDecision.events)
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

        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
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

        assertEquals(listOf(Gt7Ps5RemainingFuelLapsWarning(0)), firstWarningDecision.events)
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

        assertEquals(listOf(Gt7Ps5RemainingFuelWarning(30)), decision.events)
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

            assertEquals(listOf(Gt7Ps5RemainingFuelWarning(percent)), decision.events)
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

        assertEquals(listOf(Gt7Ps5RemainingFuelWarning(30)), decision.events)
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
        assertEquals(listOf(Gt7Ps5RemainingFuelWarning(20)), rewarnedDecision.events)
    }

    @Test
    fun `燃料残量が無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = Gt7Ps5NarratorState(),
                telemetry = telemetry(gasLevel = 20f, gasCapacity = 100f),
                settings = settings(enabledStates = mapOf(Gt7Ps5ReadoutItemKey.RemainingFuel.Root to false)),
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
                                Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                                Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled to false,
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

        assertEquals(listOf(Gt7Ps5TyreOverheat(95)), decision.events)
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
        assertEquals(listOf(Gt7Ps5TyreOverheat(95)), decision.events)
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
                        enabledStates = mapOf(Gt7Ps5ReadoutItemKey.TyreTemperature.Root to false),
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
                        enabledStates = mapOf(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning to false),
                        tyreTemperatureHighThresholdCelsius = Celsius(95),
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(true, decision.state.tyreOverheating)
    }

    private fun settings(
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root to true),
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
                        settings = settings(tyreTemperatureHighThresholdCelsius = Celsius(95)),
                    )
                assertEquals(listOf(Gt7Ps5TyreOverheat(rounded)), decision.events)
            }
        }
    }
}
