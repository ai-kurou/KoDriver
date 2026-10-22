package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsManualReadoutText
import kurou.kodriver.domain.engine.AceWindowsMyBestLap
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsManualReadoutItem
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class DetermineAceWindowsNarratorReadoutUseCaseTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerAceWindowsReadoutItemKeys()
    }

    private val useCase = DetermineAceWindowsNarratorReadoutUseCase()

    @Test
    fun `手動タイヤ温度は有限かつ正の最高温度を四捨五入する`() {
        assertEquals(null, useCase.currentMaxTyreTemperatureCelsius(null))
        assertEquals(null, useCase.currentMaxTyreTemperatureCelsius(AceWindowsTyreCarcassTemperatureData(emptyMap())))
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0f, -1f).forEach { value ->
            assertEquals(null, useCase.currentMaxTyreTemperatureCelsius(tyreCarcassTemperature(value)))
        }
        val data =
            AceWindowsTyreCarcassTemperatureData(
                mapOf(
                    WheelIndex.FRONT_LEFT to CelsiusReading(Float.NaN),
                    WheelIndex.FRONT_RIGHT to CelsiusReading(80f),
                    WheelIndex.REAR_LEFT to CelsiusReading(89.5f),
                    WheelIndex.REAR_RIGHT to CelsiusReading(0f),
                ),
            )
        assertEquals(90, useCase.currentMaxTyreTemperatureCelsius(data))
        assertEquals(89, useCase.currentMaxTyreTemperatureCelsius(tyreCarcassTemperature(89.4f)))
    }

    @Test
    fun `手動タイヤ温度は文言とデータなしと空欄を扱い自動警告状態を維持する`() {
        val key = AceWindowsReadoutItemKey.TyreTemperature.Root.value
        val state = AceWindowsNarratorState(tyreOverheating = true)
        listOf(
            AceWindowsManualReadoutItem(key, "温度{celsius}℃ {other}", 90) to "温度90℃ {other}",
            AceWindowsManualReadoutItem(key, "タイヤ温度{celsius}℃", null) to "タイヤ温度はデータがありません",
            AceWindowsManualReadoutItem(key, "", null) to "",
            AceWindowsManualReadoutItem(key, "", 90) to "",
        ).forEach { (item, expected) ->
            val decision = useCase.determineManualReadout(state, listOf(item))
            assertEquals(state, decision.state)
            assertEquals(expected, (decision.events.single() as AceWindowsManualReadoutText).text)
        }
    }

    @Test
    fun `手動燃料は編集文言と現在値を保存順に連結し空白と非対応キーを除外する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val state = AceWindowsNarratorState(remainingFuelWarned = true, lastRemainingFuelLaps = 2)
        val items =
            listOf(
                AceWindowsManualReadoutItem("unknown", "非対応", 1),
                AceWindowsManualReadoutItem(key, "あと{percent}% {unknown}", 42),
                AceWindowsManualReadoutItem(key, "  ", null),
                AceWindowsManualReadoutItem(key, "残量", null),
                AceWindowsManualReadoutItem(key, "上限超過", 1),
            )
        val decision = useCase.determineManualReadout(state, items)
        assertTrue(state === decision.state)
        assertEquals(
            listOf(
                AceWindowsManualReadoutText(
                    AceWindowsReadoutItemKey.RemainingFuel.Root,
                    "あと42% {unknown}、残り燃料はデータがありません",
                ),
            ),
            decision.events,
        )
    }

    @Test
    fun `手動燃料の単一項目とデータなしと空テンプレートと非対応キーを処理する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val cases =
            listOf(
                listOf(AceWindowsManualReadoutItem(key, "燃料{percent}%", 42)) to "燃料42%",
                listOf(AceWindowsManualReadoutItem(key, "燃料{percent}%", null)) to "残り燃料はデータがありません",
                listOf(
                    AceWindowsManualReadoutItem(key, "燃料{percent}%", null),
                    AceWindowsManualReadoutItem(key, "残量{percent}%", null),
                ) to "残り燃料はデータがありません、残り燃料はデータがありません",
                listOf(
                    AceWindowsManualReadoutItem(key, "", 42),
                    AceWindowsManualReadoutItem(key, "残量{percent}%", null),
                ) to "残り燃料はデータがありません",
                listOf(AceWindowsManualReadoutItem(key, "", 42)) to "",
                listOf(AceWindowsManualReadoutItem(key, "  ", null)) to "",
                listOf(AceWindowsManualReadoutItem("unknown", "非対応", 42)) to "読み上げる項目がありません",
                emptyList<AceWindowsManualReadoutItem>() to "読み上げる項目がありません",
            )
        cases.forEach { (items, text) ->
            val decision = useCase.determineManualReadout(AceWindowsNarratorState(), items)
            assertEquals(
                listOf(AceWindowsManualReadoutText(AceWindowsReadoutItemKey.RemainingFuel.Root, text)),
                decision.events,
            )
        }
    }

    @Test
    fun `手動燃料の現在値は四捨五入し未受信と非有限と範囲外はデータなしになる`() {
        assertEquals(null, useCase.currentRemainingFuelPercent(null))
        val cases =
            listOf(
                0.0 to 0,
                100.0 to 100,
                99.5 to 100,
                42.49 to 42,
                42.5 to 43,
                -0.01 to null,
                100.01 to null,
                Double.NaN to null,
                Double.POSITIVE_INFINITY to null,
                Double.NEGATIVE_INFINITY to null,
            )
        cases.forEach { (percent, expected) ->
            assertEquals(expected, useCase.currentRemainingFuelPercent(AceWindowsFuelData(FuelPercent(percent))))
        }
    }

    @Test
    fun `手動燃料周回数は現在値を文言に整形し燃料と保存順に連結しデータなしと空テンプレートを処理する`() {
        val fuelKey = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val lapsKey = AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value
        val cases =
            listOf(
                listOf(AceWindowsManualReadoutItem(lapsKey, "約{laps}周", 5)) to "約5周",
                listOf(AceWindowsManualReadoutItem(lapsKey, "約{laps}周", null)) to "残り燃料の周回数はデータがありません",
                listOf(AceWindowsManualReadoutItem(lapsKey, "", 5)) to "",
                listOf(
                    AceWindowsManualReadoutItem(fuelKey, "燃料{percent}%", 42),
                    AceWindowsManualReadoutItem(lapsKey, "約{laps}周", 5),
                ) to "燃料42%、約5周",
            )
        cases.forEach { (items, text) ->
            val decision = useCase.determineManualReadout(AceWindowsNarratorState(), items)
            assertEquals(
                listOf(AceWindowsManualReadoutText(AceWindowsReadoutItemKey.RemainingFuel.Root, text)),
                decision.events,
            )
        }
    }

    @Test
    fun `手動自己ベストラップを整形しデータなしと空テンプレートを扱う`() {
        val key = AceWindowsReadoutItemKey.MyBestLap.Root.value
        val cases =
            listOf(
                AceWindowsManualReadoutItem(key, "自己ベスト {laptime}", 83_456) to "自己ベスト 1分23秒456",
                AceWindowsManualReadoutItem(key, "", null) to "",
                AceWindowsManualReadoutItem(key, "自己ベスト {laptime}", null) to "自己ベストラップはデータがありません",
            )
        cases.forEach { (item, expected) ->
            val decision = useCase.determineManualReadout(AceWindowsNarratorState(), listOf(item))
            assertEquals(expected, decision.events.single().let { (it as AceWindowsManualReadoutText).text })
        }
        assertEquals(null, useCase.currentBestLapTimeMs(null))
        assertEquals(null, useCase.currentBestLapTimeMs(AceWindowsBestLapTimeData(0)))
        assertEquals(83_456, useCase.currentBestLapTimeMs(AceWindowsBestLapTimeData(83_456)))
    }

    @Test
    fun `手動燃料周回数の現在値は切り捨て未受信と0以下と非有限と上限超過はデータなしになる`() {
        assertEquals(null, useCase.currentRemainingFuelLaps(null))
        val cases =
            listOf(
                0.5f to 0,
                2.9f to 2,
                3f to 3,
                ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX.toFloat() to
                    ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX,
                (ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX + 1).toFloat() to
                    null,
                0f to null,
                -0.5f to null,
                Float.NaN to null,
                Float.POSITIVE_INFINITY to null,
                Int.MAX_VALUE.toFloat() to null,
            )
        cases.forEach { (laps, expected) ->
            assertEquals(expected, useCase.currentRemainingFuelLaps(AceWindowsRemainingFuelLapsData(laps)))
        }
    }

    @Test
    fun `手動読み上げは固定の項目なし文言を返し自動判定の状態を変更しない`() {
        val state = AceWindowsNarratorState(personalBestMs = 80_000, previousLapCount = 5)
        val decision = useCase.determineManualReadout(state)
        assertEquals(state, decision.state)
        assertTrue(state === decision.state)
        assertEquals(
            listOf(AceWindowsManualReadoutText(AceWindowsReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")),
            decision.events,
        )
    }

    @Test
    fun `周回数が減少したら自己ベストをリセットして新セッションの更新を読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state =
                    AceWindowsNarratorState(
                        personalBestMs = 59_000,
                        previousBestLapTimeMs = 59_000,
                        previousLapCount = 5,
                    ),
                data = bestLapTime(bestLapTimeMs = 69_000, currentLap = 1),
                settings = myBestLapSettings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
        assertEquals(Int.MAX_VALUE, first.state.personalBestMs)
        assertEquals(69_000, first.state.previousBestLapTimeMs)
        assertEquals(1, first.state.previousLapCount)

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                data = bestLapTime(bestLapTimeMs = 68_000, currentLap = 2),
                settings = myBestLapSettings(),
            )

        assertEquals(listOf(AceWindowsMyBestLap(lapTimeMs = 68_000)), second.events)
        assertEquals(68_000, second.state.personalBestMs)
        assertEquals(2, second.state.previousLapCount)
    }

    @Test
    fun `周回数が同じか増加した場合は自己ベストをリセットしない`() {
        for (currentLap in listOf(5, 6)) {
            val decision =
                useCase.determineMyBestLap(
                    state =
                        AceWindowsNarratorState(
                            personalBestMs = 59_000,
                            previousBestLapTimeMs = 69_000,
                            previousLapCount = 5,
                        ),
                    data = bestLapTime(bestLapTimeMs = 68_000, currentLap = currentLap),
                    settings = myBestLapSettings(),
                )

            assertEquals(emptyList<SpeechEvent>(), decision.events)
            assertEquals(59_000, decision.state.personalBestMs)
            assertEquals(68_000, decision.state.previousBestLapTimeMs)
            assertEquals(currentLap, decision.state.previousLapCount)
        }
    }

    @Test
    fun `自己ベスト判定の各経路で現在の周回数を記録する`() {
        val initialState = AceWindowsNarratorState(previousBestLapTimeMs = 60_000, previousLapCount = 1)
        val cases =
            listOf<Triple<AceWindowsNarratorState, Int, Map<ReadoutItemKey, Boolean>>>(
                Triple(AceWindowsNarratorState(), 59_000, emptyMap()),
                Triple(initialState, 0, emptyMap()),
                Triple(initialState, 60_000, emptyMap()),
                Triple(initialState.copy(personalBestMs = 58_000), 59_000, emptyMap()),
                Triple(initialState, 59_000, mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to false)),
                Triple(initialState, 59_000, mapOf(AceWindowsReadoutItemKey.MyBestLap.DetailEnabled to false)),
                Triple(initialState, 59_000, emptyMap()),
            )
        for ((state, current, enabledOverrides) in cases) {
            val decision =
                useCase.determineMyBestLap(
                    state = state,
                    data = bestLapTime(bestLapTimeMs = current, currentLap = 2),
                    settings = myBestLapSettings(enabledOverrides = enabledOverrides),
                )

            assertEquals(current, decision.state.previousBestLapTimeMs)
            assertEquals(2, decision.state.previousLapCount)
        }
    }

    @Test
    fun `初回の自己ベスト値では読み上げない`() {
        val decision =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(90_000, decision.state.previousBestLapTimeMs)
    }

    @Test
    fun `自己ベストが更新されたら更新後のラップタイムを持つイベントを読み上げる`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                data = bestLapTime(bestLapTimeMs = 89_000),
                settings = myBestLapSettings(),
            )

        assertEquals(listOf(AceWindowsMyBestLap(lapTimeMs = 89_000)), decision.events)
        assertEquals(89_000, decision.state.personalBestMs)
    }

    @Test
    fun `ベストラップタイムが0以下なら自己ベストラップを読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                data = bestLapTime(bestLapTimeMs = 0),
                settings = myBestLapSettings(),
            )

        assertTrue(second.events.isEmpty())
    }

    @Test
    fun `前回のベストラップタイムが0以下でも更新条件を満たせば自己ベストラップを読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 0),
                settings = myBestLapSettings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                data = bestLapTime(bestLapTimeMs = 89_000),
                settings = myBestLapSettings(),
            )

        assertEquals(listOf(AceWindowsMyBestLap(lapTimeMs = 89_000)), second.events)
    }

    @Test
    fun `前回より遅いラップタイムでは読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                data = bestLapTime(bestLapTimeMs = 91_000),
                settings = myBestLapSettings(),
            )

        assertTrue(second.events.isEmpty())
    }

    @Test
    fun `既に記録している自己ベストより遅ければ読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                data = bestLapTime(bestLapTimeMs = 89_000),
                settings = myBestLapSettings(),
            )

        val third =
            useCase.determineMyBestLap(
                state = second.state.copy(previousBestLapTimeMs = 95_000),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings = myBestLapSettings(),
            )

        assertTrue(third.events.isEmpty())
    }

    @Test
    fun `自己ベストの読み上げが無効なら読み上げない`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings =
                    myBestLapSettings(
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to false),
                    ),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                data = bestLapTime(bestLapTimeMs = 89_000),
                settings =
                    myBestLapSettings(
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to false),
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(Int.MAX_VALUE, decision.state.personalBestMs)
    }

    @Test
    fun `自己ベストのdetailPaneスイッチが無効なら読み上げない`() {
        val initialDecision =
            useCase.determineMyBestLap(
                state = AceWindowsNarratorState(),
                data = bestLapTime(bestLapTimeMs = 90_000),
                settings =
                    myBestLapSettings(
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.DetailEnabled to false),
                    ),
            )
        val decision =
            useCase.determineMyBestLap(
                state = initialDecision.state,
                data = bestLapTime(bestLapTimeMs = 89_000),
                settings =
                    myBestLapSettings(
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.DetailEnabled to false),
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(Int.MAX_VALUE, decision.state.personalBestMs)
    }

    @Test
    fun `enabledStatesが空でも例外にならずデフォルトtrueで読み上げる`() {
        val fuelDecision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 20.0),
                settings =
                    AceWindowsNarratorReadoutSettings(
                        enabledStates = emptyMap(),
                        remainingFuelThresholdPercentage = 30,
                    ),
            )
        val flagDecision =
            useCase.determineFlag(
                state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG),
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings =
                    AceWindowsNarratorReadoutSettings(
                        enabledStates = emptyMap(),
                        remainingFuelThresholdPercentage = 0,
                    ),
            )

        assertEquals(listOf(AceWindowsRemainingFuelWarning(20)), fuelDecision.events)
        assertEquals(listOf(AceWindowsBlueFlag()), flagDecision.events)
    }

    @Test
    fun `残量が閾値以下になると読み上げる`() {
        val decision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 20.0),
                settings = settings(thresholdPercentage = 30),
            )

        assertEquals(listOf(AceWindowsRemainingFuelWarning(20)), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `閾値ちょうどは低燃料扱い`() {
        val decision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 30.0),
                settings = settings(thresholdPercentage = 30),
            )

        assertEquals(listOf(AceWindowsRemainingFuelWarning(30)), decision.events)
    }

    @Test
    fun `警告状態が継続しても再度読み上げない`() {
        val state = AceWindowsNarratorState(remainingFuelWarned = true)

        val decision =
            useCase.determineRemainingFuel(
                state = state,
                data = fuel(remainingPercent = 20.0),
                settings = settings(thresholdPercentage = 30),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `残量が閾値より上に戻ると再度読み上げ可能になる`() {
        val warnedState =
            useCase
                .determineRemainingFuel(
                    state = AceWindowsNarratorState(),
                    data = fuel(remainingPercent = 20.0),
                    settings = settings(thresholdPercentage = 30),
                ).state

        val recoveredState =
            useCase
                .determineRemainingFuel(
                    state = warnedState,
                    data = fuel(remainingPercent = 50.0),
                    settings = settings(thresholdPercentage = 30),
                ).state

        val rewarnedDecision =
            useCase.determineRemainingFuel(
                state = recoveredState,
                data = fuel(remainingPercent = 20.0),
                settings = settings(thresholdPercentage = 30),
            )

        assertEquals(false, recoveredState.remainingFuelWarned)
        assertEquals(listOf(AceWindowsRemainingFuelWarning(20)), rewarnedDecision.events)
    }

    @Test
    fun `残量が0パーセントの未初期化値では読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 0.0),
                settings = settings(thresholdPercentage = 30),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.remainingFuelWarned)
    }

    @Test
    fun `残り燃料項目が無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 20.0),
                settings =
                    settings(
                        thresholdPercentage = 30,
                        enabledStates = mapOf(AceWindowsReadoutItemKey.RemainingFuel.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `残り燃料の読み上げはRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingFuel(
                state = AceWindowsNarratorState(),
                data = fuel(remainingPercent = 20.0),
                settings =
                    settings(
                        thresholdPercentage = 30,
                        enabledStates =
                            mapOf(
                                AceWindowsReadoutItemKey.RemainingFuel.Root to true,
                                AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled to false,
                            ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingFuelWarned)
    }

    @Test
    fun `燃料残り周回数が閾値以下になると整数部の周回数を読み上げる`() {
        val above = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 4.2f)
        val decision = remainingFuelLaps(state = above.state, laps = 2.9f)

        assertTrue(above.events.isEmpty())
        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(2)), decision.events)
        assertEquals(2, decision.state.lastRemainingFuelLaps)
    }

    @Test
    fun `燃料残り周回数が閾値ちょうどなら読み上げる`() {
        val decision = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 3.0f)

        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(3)), decision.events)
    }

    @Test
    fun `同じ燃料残り周回数の間は再度読み上げない`() {
        val first = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 2.9f)
        val decision = remainingFuelLaps(state = first.state, laps = 2.1f)

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastRemainingFuelLaps)
    }

    @Test
    fun `燃料残り周回数が1周減るごとに読み上げ1周未満では燃料切れを読み上げる`() {
        val first = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 2.5f)
        val second = remainingFuelLaps(state = first.state, laps = 1.5f)
        val third = remainingFuelLaps(state = second.state, laps = 0.5f)

        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(2)), first.events)
        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(1)), second.events)
        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(0)), third.events)
    }

    @Test
    fun `整数境界付近の揺れでは同じ周回数を再度読み上げない`() {
        val first = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 1.99f)
        val bounced = remainingFuelLaps(state = first.state, laps = 2.01f)
        val decision = remainingFuelLaps(state = bounced.state, laps = 1.98f)

        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(1)), first.events)
        assertTrue(bounced.events.isEmpty())
        assertEquals(1, bounced.state.lastRemainingFuelLaps)
        assertTrue(decision.events.isEmpty())
    }

    @Test
    fun `閾値以下での給油後は増えた周回数を基準に再度1周減るごとに読み上げる`() {
        val first = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 1.5f)
        val refueled = remainingFuelLaps(state = first.state, laps = 3.2f)
        val decision = remainingFuelLaps(state = refueled.state, laps = 2.9f)

        assertTrue(refueled.events.isEmpty())
        assertEquals(3, refueled.state.lastRemainingFuelLaps)
        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(2)), decision.events)
    }

    @Test
    fun `閾値を上回ると基準をリセットし再度閾値以下で読み上げる`() {
        val first = remainingFuelLaps(state = AceWindowsNarratorState(), laps = 2.5f)
        val refueled = remainingFuelLaps(state = first.state, laps = 10.0f)
        val decision = remainingFuelLaps(state = refueled.state, laps = 3.5f)

        assertEquals(null, refueled.state.lastRemainingFuelLaps)
        assertEquals(listOf(AceWindowsRemainingFuelLapsWarning(3)), decision.events)
    }

    @Test
    fun `燃料残り周回数が0以下や非有限値なら判定しない`() {
        val state = AceWindowsNarratorState(lastRemainingFuelLaps = 2)

        listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY).forEach { laps ->
            val decision = remainingFuelLaps(state = state, laps = laps)

            assertTrue(decision.events.isEmpty())
            assertEquals(state, decision.state)
        }
    }

    @Test
    fun `燃料残り周回数項目が無効なら読み上げないが基準は更新する`() {
        val decision =
            remainingFuelLaps(
                state = AceWindowsNarratorState(),
                laps = 2.5f,
                enabledStates = mapOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root to false),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastRemainingFuelLaps)
    }

    @Test
    fun `燃料残り周回数の読み上げはRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val decision =
            remainingFuelLaps(
                state = AceWindowsNarratorState(),
                laps = 2.5f,
                enabledStates =
                    mapOf(
                        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
                        AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled to false,
                    ),
            )

        assertTrue(decision.events.isEmpty())
        assertEquals(2, decision.state.lastRemainingFuelLaps)
    }

    @Test
    fun `初回観測時は読み上げない`() {
        val decision =
            useCase.determineFlag(
                state = AceWindowsNarratorState(),
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings = flagSettings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `フラッグが変化すると対応するイベントを読み上げる`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision =
            useCase.determineFlag(
                state = state,
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings = flagSettings(),
            )

        assertEquals(listOf(AceWindowsBlueFlag()), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `フラッグが変化しなければ読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.BLUE_FLAG)

        val decision =
            useCase.determineFlag(
                state = state,
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings = flagSettings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `フラッグ項目全体が無効なら読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision =
            useCase.determineFlag(
                state = state,
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings = flagSettings(enabledOverrides = mapOf(AceWindowsReadoutItemKey.Flag.Root to false)),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.BLUE_FLAG, decision.state.previousFlag)
    }

    @Test
    fun `個別のフラッグ項目が無効なら読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG)

        val decision =
            useCase.determineFlag(
                state = state,
                data = flag(AceWindowsFlagType.BLUE_FLAG),
                settings = flagSettings(enabledOverrides = mapOf(AceWindowsReadoutItemKey.Flag.BlueFlag to false)),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `NO_FLAGへの変化は読み上げない`() {
        val state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.BLUE_FLAG)

        val decision =
            useCase.determineFlag(
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

        val decision =
            useCase.determineFlag(
                state = state,
                data = flag(AceWindowsFlagType.UNKNOWN),
                settings = flagSettings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(AceWindowsFlagType.UNKNOWN, decision.state.previousFlag)
    }

    @Test
    fun `各フラッグ種別に対応するイベントを読み上げる`() {
        val expected =
            mapOf(
                AceWindowsFlagType.WHITE_FLAG to AceWindowsWhiteFlag(),
                AceWindowsFlagType.GREEN_FLAG to AceWindowsGreenFlag(),
                AceWindowsFlagType.RED_FLAG to AceWindowsRedFlag(),
                AceWindowsFlagType.BLUE_FLAG to AceWindowsBlueFlag(),
                AceWindowsFlagType.YELLOW_FLAG to AceWindowsYellowFlag(),
                AceWindowsFlagType.BLACK_FLAG to AceWindowsBlackFlag(),
                AceWindowsFlagType.BLACK_WHITE_FLAG to AceWindowsBlackWhiteFlag(),
                AceWindowsFlagType.CHECKERED_FLAG to AceWindowsCheckeredFlag(),
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG to AceWindowsOrangeCircleFlag(),
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG to AceWindowsRedYellowStripesFlag(),
            )

        expected.forEach { (flagType, event) ->
            val decision =
                useCase.determineFlag(
                    state = AceWindowsNarratorState(previousFlag = AceWindowsFlagType.NO_FLAG),
                    data = flag(flagType),
                    settings = flagSettings(),
                )
            assertEquals(listOf(event), decision.events)
        }
    }

    @Test
    fun `いずれかのタイヤが閾値以上になるとAceWindowsTyreOverheatを返す`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = AceWindowsNarratorState(),
                data = tyreCarcassTemperature(fl = 95.0f),
                settings = tyreTemperatureSettings(highThresholdCelsius = 90),
            )

        assertEquals(listOf(AceWindowsTyreOverheat(95)), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `高温状態が継続しても再度読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = AceWindowsNarratorState(tyreOverheating = true),
                data = tyreCarcassTemperature(fl = 95.0f),
                settings = tyreTemperatureSettings(highThresholdCelsius = 90),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() {
        val overheatState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = AceWindowsNarratorState(),
                    data = tyreCarcassTemperature(fl = 95.0f),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                ).state

        val cooledState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = overheatState,
                    data = tyreCarcassTemperature(fl = 85.0f),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                ).state

        val reovertDecision =
            useCase.determineTyreTemperatureOverheat(
                state = cooledState,
                data = tyreCarcassTemperature(fl = 95.0f),
                settings = tyreTemperatureSettings(highThresholdCelsius = 90),
            )

        assertEquals(false, cooledState.tyreOverheating)
        assertEquals(listOf(AceWindowsTyreOverheat(95)), reovertDecision.events)
    }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = AceWindowsNarratorState(),
                data = tyreCarcassTemperature(fl = 95.0f),
                settings =
                    tyreTemperatureSettings(
                        highThresholdCelsius = 90,
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.TyreTemperature.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `過熱警告スイッチがOFFの場合は読み上げられない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = AceWindowsNarratorState(),
                data = tyreCarcassTemperature(fl = 95.0f),
                settings =
                    tyreTemperatureSettings(
                        highThresholdCelsius = 90,
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `閾値ちょうどは高温扱い`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = AceWindowsNarratorState(),
                data = tyreCarcassTemperature(fl = 90.0f),
                settings = tyreTemperatureSettings(highThresholdCelsius = 90),
            )

        assertEquals(listOf(AceWindowsTyreOverheat(90)), decision.events)
    }

    @Test
    fun `ヒステリシス範囲内に下がっただけでは過熱状態を維持し再度読み上げない`() {
        val overheatState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = AceWindowsNarratorState(),
                    data = tyreCarcassTemperature(fl = 95.0f),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                ).state

        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = overheatState,
                data = tyreCarcassTemperature(fl = 87.0f),
                settings = tyreTemperatureSettings(highThresholdCelsius = 90),
            )

        assertEquals(true, decision.state.tyreOverheating)
        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `ヒステリシス下限まで下がると再度読み上げ可能になる`() {
        val overheatState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = AceWindowsNarratorState(),
                    data = tyreCarcassTemperature(fl = 95.0f),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                ).state

        val cooledState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = overheatState,
                    data = tyreCarcassTemperature(fl = 85.0f),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                ).state

        assertEquals(false, cooledState.tyreOverheating)
    }

    @Test
    fun `閾値内に車両が入ると読み上げる`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(),
                data = vehicleApproach(distanceMeters = 5.0),
                settings = vehicleApproachSettings(thresholdMeters = 10.0),
            )

        assertEquals(listOf(AceWindowsVehicleApproach()), decision.events)
        assertEquals(true, decision.state.vehicleApproaching)
    }

    @Test
    fun `閾値より遠い場合は読み上げない`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(),
                data = vehicleApproach(distanceMeters = 20.0),
                settings = vehicleApproachSettings(thresholdMeters = 10.0),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.vehicleApproaching)
    }

    @Test
    fun `接近状態が継続しても再度読み上げない`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(vehicleApproaching = true),
                data = vehicleApproach(distanceMeters = 5.0),
                settings = vehicleApproachSettings(thresholdMeters = 10.0),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.vehicleApproaching)
    }

    @Test
    fun `車両が閾値外に離れると再度読み上げ可能になる`() {
        val approachingState =
            useCase
                .determineVehicleApproach(
                    state = AceWindowsNarratorState(),
                    data = vehicleApproach(distanceMeters = 5.0),
                    settings = vehicleApproachSettings(thresholdMeters = 10.0),
                ).state

        val departedState =
            useCase
                .determineVehicleApproach(
                    state = approachingState,
                    data = vehicleApproach(distanceMeters = 20.0),
                    settings = vehicleApproachSettings(thresholdMeters = 10.0),
                ).state

        val reapproachDecision =
            useCase.determineVehicleApproach(
                state = departedState,
                data = vehicleApproach(distanceMeters = 5.0),
                settings = vehicleApproachSettings(thresholdMeters = 10.0),
            )

        assertEquals(false, departedState.vehicleApproaching)
        assertEquals(listOf(AceWindowsVehicleApproach()), reapproachDecision.events)
    }

    @Test
    fun `VEHICLE_APPROACHが無効なら読み上げない`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(),
                data = vehicleApproach(distanceMeters = 5.0),
                settings =
                    vehicleApproachSettings(
                        thresholdMeters = 10.0,
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.VehicleApproach.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.vehicleApproaching)
    }

    @Test
    fun `接近開始時の読み上げが無効なら読み上げない`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(),
                data = vehicleApproach(distanceMeters = 5.0),
                settings =
                    vehicleApproachSettings(
                        thresholdMeters = 10.0,
                        enabledOverrides = mapOf(AceWindowsReadoutItemKey.VehicleApproach.StartReadout to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.vehicleApproaching)
    }

    @Test
    fun `RootとStartReadoutがともに無効なら接近しても読み上げない`() {
        val decision =
            useCase.determineVehicleApproach(
                state = AceWindowsNarratorState(),
                data = vehicleApproach(distanceMeters = 5.0),
                settings =
                    vehicleApproachSettings(
                        thresholdMeters = 10.0,
                        enabledOverrides =
                            mapOf(
                                AceWindowsReadoutItemKey.VehicleApproach.Root to false,
                                AceWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                            ),
                    ),
            )
        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.vehicleApproaching)
    }

    private fun bestLapTime(
        bestLapTimeMs: Int,
        currentLap: Int = 0,
    ) = AceWindowsBestLapTimeData(bestLapTimeMs = bestLapTimeMs, currentLap = currentLap)

    private fun myBestLapSettings(enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap()) =
        AceWindowsNarratorReadoutSettings(
            enabledStates = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to true) + enabledOverrides,
            remainingFuelThresholdPercentage = 0,
        )

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = FuelPercent(remainingPercent))

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun vehicleApproach(distanceMeters: Double) =
        AceWindowsVehicleApproachData(nearbyVehicles = listOf(AceWindowsNearbyVehicleData(distanceMeters)))

    private fun vehicleApproachSettings(
        thresholdMeters: Double,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates =
            mapOf(
                AceWindowsReadoutItemKey.VehicleApproach.Root to true,
                AceWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
            ) + enabledOverrides,
        remainingFuelThresholdPercentage = 0,
        vehicleApproachThresholdMeters = thresholdMeters,
    )

    private fun tyreCarcassTemperature(fl: Float) =
        AceWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(fl)))

    private fun tyreTemperatureSettings(
        highThresholdCelsius: Int,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates =
            mapOf(
                AceWindowsReadoutItemKey.TyreTemperature.Root to true,
                AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
            ) + enabledOverrides,
        remainingFuelThresholdPercentage = 0,
        tyreTemperatureHighThresholdCelsius = Celsius(highThresholdCelsius),
    )

    private fun settings(
        thresholdPercentage: Int,
        enabledStates: Map<ReadoutItemKey, Boolean> = mapOf(AceWindowsReadoutItemKey.RemainingFuel.Root to true),
    ) = AceWindowsNarratorReadoutSettings(
        enabledStates = enabledStates,
        remainingFuelThresholdPercentage = thresholdPercentage,
    )

    private fun flagSettings(enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap()) =
        AceWindowsNarratorReadoutSettings(
            enabledStates =
                mapOf(
                    AceWindowsReadoutItemKey.Flag.Root to true,
                    AceWindowsReadoutItemKey.Flag.WhiteFlag to true,
                    AceWindowsReadoutItemKey.Flag.GreenFlag to true,
                    AceWindowsReadoutItemKey.Flag.RedFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlueFlag to true,
                    AceWindowsReadoutItemKey.Flag.YellowFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlackFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlackWhiteFlag to true,
                    AceWindowsReadoutItemKey.Flag.CheckeredFlag to true,
                    AceWindowsReadoutItemKey.Flag.OrangeCircleFlag to true,
                    AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag to true,
                ) + enabledOverrides,
            remainingFuelThresholdPercentage = 0,
        )

    private fun remainingFuelLaps(
        state: AceWindowsNarratorState,
        laps: Float,
        enabledStates: Map<ReadoutItemKey, Boolean> =
            mapOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true),
    ) = useCase.determineRemainingFuelLaps(
        state = state,
        data = AceWindowsRemainingFuelLapsData(remainingLaps = laps),
        settings =
            AceWindowsNarratorReadoutSettings(
                enabledStates = enabledStates,
                remainingFuelThresholdPercentage = 0,
                remainingFuelLapsThreshold = 3,
            ),
    )

    @Test
    fun `タイヤ過熱は全輪の最大カーカス温度を四捨五入する`() {
        listOf(110.49f to 110, 110.5f to 111).forEach { (maximum, expected) ->
            val decision =
                useCase.determineTyreTemperatureOverheat(
                    state = AceWindowsNarratorState(),
                    data =
                        AceWindowsTyreCarcassTemperatureData(
                            wheels =
                                mapOf(
                                    WheelIndex.FRONT_LEFT to CelsiusReading(95.0f),
                                    WheelIndex.FRONT_RIGHT to CelsiusReading(99.0f),
                                    WheelIndex.REAR_LEFT to CelsiusReading(maximum),
                                    WheelIndex.REAR_RIGHT to CelsiusReading(100.0f),
                                ),
                        ),
                    settings = tyreTemperatureSettings(highThresholdCelsius = 90),
                )
            assertEquals(listOf(AceWindowsTyreOverheat(expected)), decision.events)
        }
    }

    @Test
    fun `燃料残量は判定時の実残量を四捨五入してイベントに保持する`() {
        listOf(29.6 to 30, 0.4 to 0, 30.0 to 30, 29.5 to 30, 29.4 to 29).forEach { (remaining, percent) ->
            val decision =
                useCase.determineRemainingFuel(
                    state = AceWindowsNarratorState(),
                    data = fuel(remainingPercent = remaining),
                    settings = settings(thresholdPercentage = 30),
                )
            assertEquals(listOf(AceWindowsRemainingFuelWarning(percent)), decision.events)
            assertEquals(true, decision.state.remainingFuelWarned)
        }
    }

    @Test
    fun `燃料残量イベントのパーセントは100を上限にする`() {
        listOf(99.6, 100.0, 100.6).forEach { remaining ->
            val decision =
                useCase.determineRemainingFuel(
                    state = AceWindowsNarratorState(),
                    data = fuel(remainingPercent = remaining),
                    settings = settings(thresholdPercentage = 101),
                )
            assertEquals(listOf(AceWindowsRemainingFuelWarning(100)), decision.events)
        }
    }
}
