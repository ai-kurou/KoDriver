package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("TooManyFunctions")
class DetermineLmuWindowsNarratorReadoutUseCaseTest {
    private val useCase = DetermineLmuWindowsNarratorReadoutUseCase()

    @Test
    fun `周回数が減少したら自己ベストをリセットして新セッションの更新を読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state =
                    LmuWindowsNarratorState(
                        personalBestMs = 59_000L,
                        previousBestLapTimeMs = 59_000L,
                        previousLapCount = 5,
                    ),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 69_000L),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
        assertEquals(Long.MAX_VALUE, first.state.personalBestMs)
        assertEquals(69_000L, first.state.previousBestLapTimeMs)
        assertEquals(1, first.state.previousLapCount)

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 68_000L),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsMyBestLap(68_000L)), second.events)
        assertEquals(68_000L, second.state.personalBestMs)
        assertEquals(2, second.state.previousLapCount)
    }

    @Test
    fun `周回数が同じか増加した場合は自己ベストをリセットしない`() {
        for (currentLap in listOf(5, 6)) {
            val decision =
                useCase.determineMyBestLap(
                    state =
                        LmuWindowsNarratorState(
                            personalBestMs = 59_000L,
                            previousBestLapTimeMs = 69_000L,
                            previousLapCount = 5,
                        ),
                    telemetry = lapTelemetry(currentLap = currentLap, bestLapTimeMs = 68_000L),
                    settings = settings(),
                )

            assertEquals(emptyList<SpeechEvent>(), decision.events)
            assertEquals(59_000L, decision.state.personalBestMs)
            assertEquals(68_000L, decision.state.previousBestLapTimeMs)
            assertEquals(currentLap, decision.state.previousLapCount)
        }
    }

    @Test
    fun `自己ベスト判定の各経路で現在の周回数を記録する`() {
        val initialState = LmuWindowsNarratorState(previousBestLapTimeMs = 60_000L, previousLapCount = 1)
        val cases =
            listOf<Triple<LmuWindowsNarratorState, Long, Map<ReadoutItemKey, Boolean>>>(
                Triple(LmuWindowsNarratorState(), 59_000L, emptyMap()),
                Triple(initialState, 0L, emptyMap()),
                Triple(initialState, 60_000L, emptyMap()),
                Triple(initialState.copy(personalBestMs = 58_000L), 59_000L, emptyMap()),
                Triple(initialState, 59_000L, mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to false)),
                Triple(initialState, 59_000L, mapOf(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled to false)),
                Triple(initialState, 59_000L, emptyMap()),
            )
        for ((state, current, enabledStates) in cases) {
            val decision =
                useCase.determineMyBestLap(
                    state = state,
                    telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = current),
                    settings = settings(enabledStates = enabledStates),
                )

            assertEquals(current, decision.state.previousBestLapTimeMs)
            assertEquals(2, decision.state.previousLapCount)
        }
    }

    @Test
    fun `enabledStatesが空でも例外にならずデフォルト値で判定する`() {
        val emptySettings = settings(enabledStates = emptyMap())

        val myBestLap =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(previousBestLapTimeMs = 60_000L),
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings = emptySettings,
            )
        val vehicleApproach =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = emptySettings,
                observedAtMs = 0L,
            )
        val vehicleDamage =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
                vehicleDamage = damage(overheating = true),
                settings = emptySettings,
            )
        val tyreTemperatureOverheat =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 95.0),
                settings = emptySettings.copy(tyreTemperatureHighThresholdCelsius = Celsius(90)),
            )
        val tyreTemperatureLow =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings = emptySettings,
            )
        val tyreWear =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.4),
                settings = emptySettings.copy(tyreWearThresholdPercentage = 50),
            )
        val remainingVirtualEnergy =
            useCase.determineRemainingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings = emptySettings.copy(remainingVirtualEnergyThresholdPercentage = 50),
            )
        val pitTimingVirtualEnergy =
            useCase.determinePitTimingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = emptySettings,
                observedAtMs = 0L,
            )
        val pitTimingTyreWear =
            useCase.determinePitTimingTyreWear(
                state = LmuWindowsNarratorState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 1.0),
                settings = emptySettings,
                observedAtMs = 0L,
            )
        val raceFlags =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(previousRaceFlags = clearFlags()),
                raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
                settings = emptySettings,
            )

        // LmuWindows.VehicleDamage.RootはREADOUT_ENABLED_STATE_DEFAULTでfalseのため読み上げない。
        assertEquals(emptyList<SpeechEvent>(), vehicleDamage.events)
        // それ以外の項目はデフォルトtrue/READOUT_ENABLED_STATE_DEFAULT準拠で判定され、例外は投げない。
        assertNotNull(myBestLap)
        assertNotNull(vehicleApproach)
        assertNotNull(tyreTemperatureOverheat)
        assertNotNull(tyreTemperatureLow)
        assertNotNull(tyreWear)
        assertNotNull(remainingVirtualEnergy)
        assertNotNull(pitTimingVirtualEnergy)
        assertNotNull(pitTimingTyreWear)
        assertNotNull(raceFlags)
    }

    @Test
    fun `初回の自己ベストラップは状態だけ更新する`() {
        val decision =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(60_000L, decision.state.previousBestLapTimeMs)
    }

    @Test
    fun `自己ベストラップが更新されると更新後のタイムを持つイベントを返す`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsMyBestLap(59_000L)), second.events)
        assertEquals(59_000L, second.state.personalBestMs)
    }

    @Test
    fun `ベストラップタイムが0以下なら自己ベストラップを読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 0L),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `前回のベストラップタイムが0以下でも更新条件を満たせば自己ベストラップを読み上げる`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 0L),
                settings = settings(),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsMyBestLap(59_000L)), second.events)
    }

    @Test
    fun `前回より遅いラップタイムでは読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 61_000L),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `既に記録している自己ベストより遅ければ読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )
        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings = settings(),
            )

        val third =
            useCase.determineMyBestLap(
                state = second.state.copy(previousBestLapTimeMs = 65_000L),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), third.events)
    }

    @Test
    fun `自己ベストラップ項目が無効なら読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings =
                    settings(
                        enabledStates = allEnabledStates + mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to false),
                    ),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings =
                    settings(
                        enabledStates = allEnabledStates + mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `自己ベストラップの読み上げはRootが有効でもdetailPane側のスイッチが無効なら読み上げない`() {
        val first =
            useCase.determineMyBestLap(
                state = LmuWindowsNarratorState(),
                telemetry = telemetry(bestLapTimeMs = 60_000L),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled to false),
                    ),
            )

        val second =
            useCase.determineMyBestLap(
                state = first.state,
                telemetry = telemetry(bestLapTimeMs = 59_000L),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左接近が50ms継続するとCarLeftを返す`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 50L,
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
        assertEquals(listOf(LmuWindowsCarLeft()), second.events)
    }

    @Test
    fun `右接近の開始時はCarRightを返す`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = rightVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = rightVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 50L,
            )

        assertEquals(listOf(LmuWindowsCarRight()), second.events)
    }

    @Test
    fun `左接近の開始時はCarLeftを返す`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 50L,
            )

        assertEquals(listOf(LmuWindowsCarLeft()), second.events)
    }

    @Test
    fun `50ms未満の接近では読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(),
                observedAtMs = 49L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左右同時接近は読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftAndRightVehicleApproach(),
                settings = settings(),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftAndRightVehicleApproach(),
                settings = settings(),
                observedAtMs = 50L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `接近読み上げ無効時は状態だけ更新する`() {
        val decision =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                                ),
                    ),
                observedAtMs = 0L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertNotNull(decision.state.vehicleApproachState.left[1])
    }

    @Test
    fun `1周目スキップ中の0周目は接近を読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(skipFirstLap = true, currentLap = 0),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(skipFirstLap = true, currentLap = 0),
                observedAtMs = 50L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `車両接近項目が無効なら接近を読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                    ),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                    ),
                observedAtMs = 50L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左接近が閾値秒数継続するとCarLeftSustainedを返す`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        assertEquals(listOf(LmuWindowsCarLeft(), LmuWindowsCarLeftSustained()), second.events)
    }

    @Test
    fun `右接近が閾値秒数継続するとCarRightSustainedを返す`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = rightVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = rightVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        assertEquals(listOf(LmuWindowsCarRight(), LmuWindowsCarRightSustained()), second.events)
    }

    @Test
    fun `左右同時に継続接近しても読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftAndRightVehicleApproach(),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftAndRightVehicleApproach(),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `閾値秒数未満では継続接近を読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 6_999L,
            )

        assertEquals(listOf(LmuWindowsCarLeft()), second.events)
    }

    @Test
    fun `接近継続時の読み上げが無効なら継続接近を読み上げない`() {
        val disabledStates =
            allEnabledStates +
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false,
                )
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        assertEquals(listOf(LmuWindowsCarLeft()), second.events)
    }

    @Test
    fun `車両接近項目が無効なら継続接近も読み上げない`() {
        val disabledStates =
            allEnabledStates +
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root to false,
                )
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )

        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `一度読み上げた継続接近は同じ側の接近が続いても再度読み上げない`() {
        val first =
            useCase.determineVehicleApproach(
                state = LmuWindowsNarratorState(),
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 0L,
            )
        val second =
            useCase.determineVehicleApproach(
                state = first.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 7_000L,
            )

        val third =
            useCase.determineVehicleApproach(
                state = second.state,
                vehicleApproach = leftVehicleApproach(vehicleId = 1),
                settings = settings(sustainedApproachDurationSeconds = 7),
                observedAtMs = 14_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), third.events)
    }

    @Test
    fun `初回の旗情報は状態だけ更新する`() {
        val decision =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(playerFlag = PrimaryFlag.BLUE),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(PrimaryFlag.BLUE, decision.state.previousRaceFlags?.playerFlag)
    }

    @Test
    fun `旗の変化を読み上げイベントに変換する`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags =
                    clearFlags(
                        gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                        playerFlag = PrimaryFlag.BLUE,
                        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
                    ),
                settings = settings(),
            )

        assertEquals(
            listOf(
                LmuWindowsBlueFlag(),
                LmuWindowsYellowFlag(),
                LmuWindowsFullCourseYellow(),
            ),
            second.events,
        )
    }

    @Test
    fun `赤旗の変化をRedFlagイベントに変換する`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsRedFlag()), second.events)
    }

    @Test
    fun `青旗が継続しても再度読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(playerFlag = PrimaryFlag.BLUE),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags = clearFlags(playerFlag = PrimaryFlag.BLUE),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `イエローセクターが継続しても再度読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags =
                    clearFlags(
                        sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                    ),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags =
                    clearFlags(
                        sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                    ),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `フルコースイエロー項目が無効なら読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.Flag.FullCourseYellow to false),
                    ),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags = clearFlags(gamePhase = SessionPhase.FULL_COURSE_YELLOW),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.Flag.FullCourseYellow to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `フルコースイエローが継続しても再度読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(gamePhase = SessionPhase.FULL_COURSE_YELLOW),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags = clearFlags(gamePhase = SessionPhase.FULL_COURSE_YELLOW),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `赤旗が継続しても再度読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `無効な旗項目は読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags =
                    clearFlags(
                        gamePhase = SessionPhase.RED_FLAG,
                        playerFlag = PrimaryFlag.BLUE,
                        sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                    ),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.Flag.BlueFlag to false,
                                    LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to false,
                                    LmuWindowsReadoutItemKey.Flag.RedFlag to false,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `フラッグ項目が無効なら詳細フラッグ項目が有効でも読み上げない`() {
        val first =
            useCase.determineRaceFlags(
                state = LmuWindowsNarratorState(),
                raceFlags = clearFlags(),
                settings = settings(),
            )

        val second =
            useCase.determineRaceFlags(
                state = first.state,
                raceFlags =
                    clearFlags(
                        gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                        playerFlag = PrimaryFlag.BLUE,
                        sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                    ),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.Flag.Root to false,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
        assertEquals(SessionPhase.FULL_COURSE_YELLOW, second.state.previousRaceFlags?.gamePhase)
    }

    @Test
    fun `初回の車両故障情報は状態だけ更新する`() {
        val decision =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(),
                vehicleDamage = damage(overheating = true),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.previousVehicleDamage?.overheating)
    }

    @Test
    fun `オーバーヒートがfalseからtrueに変化するとOverheatingを返す`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(),
                vehicleDamage = damage(overheating = false),
                settings = settings(),
            )

        val second =
            useCase.determineVehicleDamage(
                state = first.state,
                vehicleDamage = damage(overheating = true),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsOverheating()), second.events)
    }

    @Test
    fun `オーバーヒートが継続しても再度読み上げない`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = true)),
                vehicleDamage = damage(overheating = true),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
    }

    @Test
    fun `オーバーヒートしていない状態が継続しても読み上げない`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(),
                vehicleDamage = damage(overheating = false),
                settings = settings(),
            )

        val second =
            useCase.determineVehicleDamage(
                state = first.state,
                vehicleDamage = damage(overheating = false),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `オーバーヒート項目が無効なら読み上げない`() {
        val decision =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
                vehicleDamage = damage(overheating = true),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `車両故障項目が無効なら読み上げない`() {
        val decision =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
                vehicleDamage = damage(overheating = true),
                settings =
                    settings(
                        enabledStates = allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `部品脱落がfalseからtrueに変化するとPartDetachedを返す`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(),
                vehicleDamage = damage(partDetached = false),
                settings = settings(),
            )

        val second =
            useCase.determineVehicleDamage(
                state = first.state,
                vehicleDamage = damage(partDetached = true),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsPartDetached()), second.events)
    }

    @Test
    fun `部品脱落が継続しても再度読み上げない`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(partDetached = true)),
                vehicleDamage = damage(partDetached = true),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
    }

    @Test
    fun `部品脱落項目が無効なら読み上げない`() {
        val decision =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(previousVehicleDamage = damage(partDetached = false)),
                vehicleDamage = damage(partDetached = true),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `オーバーヒートと部品脱落が同時にtrueへ変化すると両方読み上げる`() {
        val first =
            useCase.determineVehicleDamage(
                state = LmuWindowsNarratorState(),
                vehicleDamage = damage(overheating = false, partDetached = false),
                settings = settings(),
            )

        val second =
            useCase.determineVehicleDamage(
                state = first.state,
                vehicleDamage = damage(overheating = true, partDetached = true),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsOverheating(), LmuWindowsPartDetached()), second.events)
    }

    @Test
    fun `初回のデータでは読み上げず previousTyreDetached を保存する`() {
        val decision =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(),
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(tyreDetached(WheelIndex.FRONT_LEFT), decision.state.previousTyreDetached)
    }

    @Test
    fun `タイヤ脱落がfalseからtrueに変化するとTyreDetachedを返す`() {
        val first =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(),
                tyreDetached = tyreDetached(),
                settings = settings(),
            )

        val second =
            useCase.determineTyreDetached(
                state = first.state,
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsTyreDetached()), second.events)
    }

    @Test
    fun `タイヤ脱落が継続しても再度読み上げない`() {
        val first =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(previousTyreDetached = tyreDetached(WheelIndex.FRONT_LEFT)),
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), first.events)
    }

    @Test
    fun `タイヤ脱落項目が無効なら読み上げない`() {
        val decision =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(previousTyreDetached = tyreDetached()),
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `VehicleDamage項目全体が無効なら読み上げない`() {
        val decision =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(previousTyreDetached = tyreDetached()),
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings =
                    settings(
                        enabledStates = allEnabledStates + mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `別のタイヤが新たに脱落しても再度読み上げる`() {
        val first =
            useCase.determineTyreDetached(
                state = LmuWindowsNarratorState(),
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                settings = settings(),
            )

        val second =
            useCase.determineTyreDetached(
                state = first.state,
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT, WheelIndex.REAR_RIGHT),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsTyreDetached()), second.events)
    }

    @Test
    fun `いずれかのタイヤが閾値以上になると TyreOverheat を返す`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 95.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(listOf(LmuWindowsTyreOverheat(95)), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `過熱と低温の警告は全輪の最高温度を四捨五入する`() {
        listOf(99.5 to 100, 99.4 to 99).forEach { (temperature, expected) ->
            WheelIndex.entries.forEach { hottestWheel ->
                val input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels =
                                    WheelIndex.entries.associateWith {
                                        CelsiusReading(if (it == hottestWheel) temperature.toFloat() else 55f)
                                    },
                            ),
                        raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
                    )
                val overheat =
                    useCase.determineTyreTemperatureOverheat(
                        LmuWindowsNarratorState(),
                        input,
                        settings(tyreTemperatureHighThresholdCelsius = 90),
                    )
                val cold =
                    useCase.determineTyreTemperatureLow(
                        LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                        input,
                        settings(),
                    )
                assertEquals(listOf(LmuWindowsTyreOverheat(expected)), overheat.events)
                assertEquals(listOf(LmuWindowsTyreCold(expected)), cold.events)
            }
        }
    }

    @Test
    fun `高温状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(tyreOverheating = true)
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = state,
                input = tyreTemperatureInput(fl = 95.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() {
        val overheatState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = LmuWindowsNarratorState(),
                    input = tyreTemperatureInput(fl = 95.0),
                    settings = settings(tyreTemperatureHighThresholdCelsius = 90),
                ).state

        val cooledState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = overheatState,
                    input = tyreTemperatureInput(fl = 85.0),
                    settings = settings(tyreTemperatureHighThresholdCelsius = 90),
                ).state

        val reovertState =
            useCase.determineTyreTemperatureOverheat(
                state = cooledState,
                input = tyreTemperatureInput(fl = 95.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(false, cooledState.tyreOverheating)
        assertEquals(listOf(LmuWindowsTyreOverheat(95)), reovertState.events)
    }

    @Test
    fun `ヒステリシス範囲内の初期温度では過熱状態にならない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 87.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.tyreOverheating)
    }

    @Test
    fun `タイヤ温度項目が無効なら過熱警告スイッチがONでも読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 95.0),
                settings =
                    settings(
                        tyreTemperatureHighThresholdCelsius = 90,
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `無効中に過熱した状態で再有効化しても読み上げない`() {
        val disabledState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = LmuWindowsNarratorState(),
                    input = tyreTemperatureInput(fl = 95.0),
                    settings =
                        settings(
                            tyreTemperatureHighThresholdCelsius = 90,
                            enabledStates =
                                allEnabledStates + mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to false),
                        ),
                ).state

        val reenabledDecision =
            useCase.determineTyreTemperatureOverheat(
                state = disabledState,
                input = tyreTemperatureInput(fl = 95.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(emptyList<SpeechEvent>(), reenabledDecision.events)
    }

    @Test
    fun `過熱警告スイッチがOFFの場合は読み上げられない`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 95.0),
                settings =
                    settings(
                        tyreTemperatureHighThresholdCelsius = 90,
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `いずれかのタイヤの残存率が閾値以下になると TyreWearWarning を返す`() {
        val decision =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.4),
                settings = settings(tyreWearThresholdPercentage = 50),
            )

        assertEquals(listOf(LmuWindowsTyreWearWarning(50)), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `摩耗警告イベントは実測残存率ではなく設定閾値を保持する`() {
        val decision =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.4),
                settings = settings(tyreWearThresholdPercentage = 70),
            )

        assertEquals(listOf(LmuWindowsTyreWearWarning(70)), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `摩耗警告状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(tyreWearWarned = true)
        val decision =
            useCase.determineTyreWear(
                state = state,
                data = tyreWear(fl = 0.4),
                settings = settings(tyreWearThresholdPercentage = 50),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `全タイヤが閾値を上回ると再度読み上げ可能になる`() {
        val warnedState =
            useCase
                .determineTyreWear(
                    state = LmuWindowsNarratorState(),
                    data = tyreWear(fl = 0.4),
                    settings = settings(tyreWearThresholdPercentage = 50),
                ).state

        val recoveredState =
            useCase
                .determineTyreWear(
                    state = warnedState,
                    data = tyreWear(fl = 0.6),
                    settings = settings(tyreWearThresholdPercentage = 50),
                ).state

        val rewarnedDecision =
            useCase.determineTyreWear(
                state = recoveredState,
                data = tyreWear(fl = 0.4),
                settings = settings(tyreWearThresholdPercentage = 50),
            )

        assertEquals(false, recoveredState.tyreWearWarned)
        assertEquals(listOf(LmuWindowsTyreWearWarning(50)), rewarnedDecision.events)
    }

    @Test
    fun `いずれのタイヤも閾値を上回るなら読み上げない`() {
        val decision =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.9),
                settings = settings(tyreWearThresholdPercentage = 50),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.tyreWearWarned)
    }

    @Test
    fun `タイヤ摩耗項目が無効なら読み上げない`() {
        val decision =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.4),
                settings =
                    settings(
                        tyreWearThresholdPercentage = 50,
                        enabledStates = allEnabledStates + mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `タイヤ摩耗のdetailPaneスイッチが無効なら読み上げない`() {
        val decision =
            useCase.determineTyreWear(
                state = LmuWindowsNarratorState(),
                data = tyreWear(fl = 0.4),
                settings =
                    settings(
                        tyreWearThresholdPercentage = 50,
                        enabledStates =
                            allEnabledStates +
                                mapOf(LmuWindowsReadoutItemKey.TyreWear.WarningReadout to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `いずれかのブレーキが閾値以上になると BrakeOverheat を返す`() {
        val decision =
            useCase.determineBrakeTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                data = brakeTemperature(fl = 750.0),
                settings = settings(brakeTemperatureHighThresholdCelsius = 650),
            )

        assertEquals(listOf(LmuWindowsBrakeOverheat(650)), decision.events)
        assertEquals(true, decision.state.brakeOverheating)
    }

    @Test
    fun `ブレーキ高温状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(brakeOverheating = true)
        val decision =
            useCase.determineBrakeTemperatureOverheat(
                state = state,
                data = brakeTemperature(fl = 750.0),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.brakeOverheating)
    }

    @Test
    fun `いずれかのブレーキ残量が閾値以下になると BrakeWearLow を返す`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(),
                data = brakeWear(fl = 18),
                settings = settings(brakeWearLowThresholdPercent = 20),
            )

        assertEquals(listOf(LmuWindowsBrakeWearLow(20)), decision.events)
        assertEquals(true, decision.state.brakeWearWarned)
    }

    @Test
    fun `ブレーキ残量が閾値ちょうどでも BrakeWearLow を返す`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(),
                data = brakeWear(rr = 20),
                settings = settings(brakeWearLowThresholdPercent = 20),
            )

        assertEquals(listOf(LmuWindowsBrakeWearLow(20)), decision.events)
    }

    @Test
    fun `全ブレーキの残量が閾値を超えていれば読み上げない`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(),
                data = brakeWear(),
                settings = settings(brakeWearLowThresholdPercent = 20),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.brakeWearWarned)
    }

    @Test
    fun `ブレーキ残量低下が継続しても再度読み上げない`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(brakeWearWarned = true),
                data = brakeWear(fl = 10),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.brakeWearWarned)
    }

    @Test
    fun `ブレーキ交換で残量が閾値を超えると再度読み上げ可能になる`() {
        val warnedState =
            useCase
                .determineBrakeWearLow(LmuWindowsNarratorState(), brakeWear(fl = 10), settings())
                .state
        val replacedState = useCase.determineBrakeWearLow(warnedState, brakeWear(), settings()).state
        val decision = useCase.determineBrakeWearLow(replacedState, brakeWear(fl = 10), settings())

        assertEquals(false, replacedState.brakeWearWarned)
        assertEquals(listOf(LmuWindowsBrakeWearLow(20)), decision.events)
    }

    @Test
    fun `ブレーキ摩耗のルートスイッチが無効だと読み上げない`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(),
                data = brakeWear(fl = 10),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.brakeWearWarned)
    }

    @Test
    fun `ブレーキ摩耗の警告スイッチが無効だと読み上げない`() {
        val decision =
            useCase.determineBrakeWearLow(
                state = LmuWindowsNarratorState(),
                data = brakeWear(fl = 10),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.BrakeWear.WarningReadout to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `全ブレーキがヒステリシス下限以下に冷えると再度読み上げ可能になる`() {
        val overheatState =
            useCase
                .determineBrakeTemperatureOverheat(
                    state = LmuWindowsNarratorState(),
                    data = brakeTemperature(fl = 750.0),
                    settings = settings(),
                ).state

        val cooledState =
            useCase
                .determineBrakeTemperatureOverheat(
                    state = overheatState,
                    data = brakeTemperature(fl = 600.0),
                    settings = settings(),
                ).state

        val reovertDecision =
            useCase.determineBrakeTemperatureOverheat(
                state = cooledState,
                data = brakeTemperature(fl = 750.0),
                settings = settings(),
            )

        assertEquals(false, cooledState.brakeOverheating)
        assertEquals(listOf(LmuWindowsBrakeOverheat(700)), reovertDecision.events)
    }

    @Test
    fun `ヒステリシス下限をわずかに上回る温度では過熱状態を維持し再度読み上げない`() {
        val overheatState =
            useCase
                .determineBrakeTemperatureOverheat(
                    state = LmuWindowsNarratorState(),
                    data = brakeTemperature(fl = 750.0),
                    settings = settings(),
                ).state

        val cooledDecision =
            useCase.determineBrakeTemperatureOverheat(
                state = overheatState,
                data = brakeTemperature(fl = 601.0),
                settings = settings(),
            )

        val decision =
            useCase.determineBrakeTemperatureOverheat(
                state = cooledDecision.state,
                data = brakeTemperature(fl = 750.0),
                settings = settings(),
            )

        assertEquals(true, cooledDecision.state.brakeOverheating)
        assertEquals(emptyList<SpeechEvent>(), cooledDecision.events)
        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `ブレーキ温度項目が無効なら読み上げない`() {
        val decision =
            useCase.determineBrakeTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                data = brakeTemperature(fl = 750.0),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates + mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.brakeOverheating)
    }

    @Test
    fun `ブレーキ温度のdetailPaneスイッチが無効なら読み上げない`() {
        val decision =
            useCase.determineBrakeTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                data = brakeTemperature(fl = 750.0),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.brakeOverheating)
    }

    @Test
    fun `残量が閾値以下になると RemainingVirtualEnergyWarning を返す`() {
        val decision =
            useCase.determineRemainingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
            )

        assertEquals(listOf(LmuWindowsRemainingVirtualEnergyWarning(50)), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `残量警告状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(remainingVirtualEnergyWarned = true)
        val decision =
            useCase.determineRemainingVirtualEnergy(
                state = state,
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `残量が閾値より上に戻ると再度読み上げ可能になる`() {
        val warnedState =
            useCase
                .determineRemainingVirtualEnergy(
                    state = LmuWindowsNarratorState(),
                    data = remainingVirtualEnergy(remainingRatio = 0.4),
                    settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
                ).state

        val recoveredState =
            useCase
                .determineRemainingVirtualEnergy(
                    state = warnedState,
                    data = remainingVirtualEnergy(remainingRatio = 0.6),
                    settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
                ).state

        val rewarnedDecision =
            useCase.determineRemainingVirtualEnergy(
                state = recoveredState,
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
            )

        assertEquals(false, recoveredState.remainingVirtualEnergyWarned)
        assertEquals(listOf(LmuWindowsRemainingVirtualEnergyWarning(50)), rewarnedDecision.events)
    }

    @Test
    fun `残量が閾値より上なら読み上げない`() {
        val decision =
            useCase.determineRemainingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                data = remainingVirtualEnergy(remainingRatio = 0.6),
                settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(false, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `バーチャルエナジー残量項目が無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings =
                    settings(
                        remainingVirtualEnergyThresholdPercentage = 50,
                        enabledStates =
                            allEnabledStates +
                                mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `バーチャルエナジー残量のdetailPaneスイッチが無効なら読み上げない`() {
        val decision =
            useCase.determineRemainingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                data = remainingVirtualEnergy(remainingRatio = 0.4),
                settings =
                    settings(
                        remainingVirtualEnergyThresholdPercentage = 50,
                        enabledStates =
                            allEnabledStates +
                                mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout to false),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `閾値ちょうどは高温扱い`() {
        val decision =
            useCase.determineTyreTemperatureOverheat(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 90.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(listOf(LmuWindowsTyreOverheat(90)), decision.events)
    }

    @Test
    fun `ヒステリシス範囲内に下がっただけでは過熱状態を維持し再度読み上げない`() {
        val overheatState =
            useCase
                .determineTyreTemperatureOverheat(
                    state = LmuWindowsNarratorState(),
                    input = tyreTemperatureInput(fl = 95.0),
                    settings = settings(tyreTemperatureHighThresholdCelsius = 90),
                ).state

        val bandState =
            useCase.determineTyreTemperatureOverheat(
                state = overheatState,
                input = tyreTemperatureInput(fl = 87.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        val reheatedDecision =
            useCase.determineTyreTemperatureOverheat(
                state = bandState.state,
                input = tyreTemperatureInput(fl = 91.0),
                settings = settings(tyreTemperatureHighThresholdCelsius = 90),
            )

        assertEquals(true, bandState.state.tyreOverheating)
        assertEquals(emptyList<SpeechEvent>(), bandState.events)
        assertEquals(emptyList<SpeechEvent>(), reheatedDecision.events)
    }

    @Test
    fun `初回のgamePhase観測では低温でも読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(SessionPhase.GARAGE, decision.state.previousGamePhaseForTyreLowWarning)
    }

    @Test
    fun `ガレージに遷移した瞬間に低温タイヤがあるとTyreColdを返す`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsTyreCold(55)), decision.events)
    }

    @Test
    fun `対象外のgamePhaseに遷移しても読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.COUNTDOWN)),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `gamePhaseが変化しなければ低温でも読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GARAGE),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `1輪でも60度以下なら読み上げる`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input =
                    tyreTemperatureInput(
                        fl = 60.0,
                        fr = 80.0,
                        rl = 80.0,
                        rr = 80.0,
                        raceFlags = clearFlags(gamePhase = SessionPhase.WARM_UP),
                    ),
                settings = settings(),
            )

        assertEquals(listOf(LmuWindowsTyreCold(80)), decision.events)
    }

    @Test
    fun `全タイヤが60度超なら読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input =
                    tyreTemperatureInput(
                        fl = 61.0,
                        fr = 80.0,
                        rl = 80.0,
                        rr = 80.0,
                        raceFlags = clearFlags(gamePhase = SessionPhase.WARM_UP),
                    ),
                settings = settings(),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `選択解除されたgamePhaseに遷移しても読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings =
                    settings().copy(
                        tyreTemperatureLowWarningPhases =
                            setOf(
                                SessionPhase.WARM_UP,
                                SessionPhase.GRID_WALK,
                                SessionPhase.FORMATION,
                            ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `低温警告スイッチがOFFの場合は読み上げられない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to false,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `タイヤ温度項目が無効なら低温警告スイッチがONでも読み上げない`() {
        val decision =
            useCase.determineTyreTemperatureLow(
                state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
                input = tyreTemperatureInput(fl = 55.0, raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE)),
                settings =
                    settings(
                        enabledStates =
                            allEnabledStates +
                                mapOf(
                                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to true,
                                ),
                    ),
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `バーチャルエナジーのピットタイミング読み上げ記録は対応する履歴だけを更新する`() {
        val state =
            pitTimingBoundaryState().copy(
                lastAnnouncedPitTimingVirtualEnergyLaps = 3,
                lastAnnouncedPitTimingTyreWearLaps = 2,
                lastPitTimingVirtualEnergyEvaluationLap = 5,
                lastPitTimingTyreWearEvaluationLap = 5,
            )

        val announcedState =
            useCase.recordPitTimingAnnounced(
                state,
                LmuWindowsPitTimingWarning(0, PitTimingSource.VirtualEnergy),
            )

        assertEquals(state.copy(lastAnnouncedPitTimingVirtualEnergyLaps = 0), announcedState)
    }

    @Test
    fun `タイヤ摩耗のピットタイミング読み上げ記録は対応する履歴だけを更新する`() {
        val state =
            pitTimingBoundaryState().copy(
                lastAnnouncedPitTimingVirtualEnergyLaps = 3,
                lastAnnouncedPitTimingTyreWearLaps = 2,
                lastPitTimingVirtualEnergyEvaluationLap = 5,
                lastPitTimingTyreWearEvaluationLap = 5,
            )

        val announcedState =
            useCase.recordPitTimingAnnounced(state, LmuWindowsPitTimingWarning(0, PitTimingSource.TyreWear))

        assertEquals(state.copy(lastAnnouncedPitTimingTyreWearLaps = 0), announcedState)
    }

    @Test
    fun `バーチャルエナジー予想残り周回数は直近に完走したラップの消費率を使って最速ラップの30秒前を過ぎたら読み上げる`() {
        val firstLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = settings(),
                observedAtMs = 0L,
            )
        val midLap1Decision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
                settings = settings(),
                observedAtMs = 45_000L,
            )
        // ラップ1完走時の消費率(0.1)が、ラップ2開始時に次回以降の推定基準として採用される。
        val lapStartDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = midLap1Decision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.8),
                settings = settings(pitTimingVirtualEnergyLapsThreshold = 3),
                observedAtMs = 90_000L,
            )
        val decision =
            useCase.determinePitTimingVirtualEnergy(
                state = lapStartDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.05),
                settings = settings(pitTimingVirtualEnergyLapsThreshold = 3),
                observedAtMs = 150_000L,
            )

        assertEquals(
            0.1,
            lapStartDecision.state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption ?: 0.0,
            1e-9,
        )
        assertEquals(
            listOf(LmuWindowsPitTimingWarning(0, source = PitTimingSource.VirtualEnergy)),
            decision.events,
        )
        assertEquals(2, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
        assertEquals(-1, decision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        val announcedState =
            useCase.recordPitTimingAnnounced(
                decision.state,
                LmuWindowsPitTimingWarning(0, PitTimingSource.VirtualEnergy),
            )
        assertEquals(0, announcedState.lastAnnouncedPitTimingVirtualEnergyLaps)
    }

    @Test
    fun `ピットタイミング項目が無効でも平均消費量の計算に成功したラップは評価済みとして記録する`() {
        val firstLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 0L,
            )
        val midLap1Decision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 45_000L,
            )
        val lapStartDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = midLap1Decision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.8),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 90_000L,
            )
        // 平均消費量(0.1)の計算に成功した後、項目が無効なため読み上げない早期returnに到達する。
        val decision =
            useCase.determinePitTimingVirtualEnergy(
                state = lapStartDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.05),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 150_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(2, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
    }

    @Test
    fun `バーチャルエナジー予想残り周回数は読み上げタイミング前なら読み上げない`() {
        val firstLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = settings(),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determinePitTimingVirtualEnergy(
                state = nextLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
                settings = settings(),
                observedAtMs = 159_999L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(-1, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
    }

    @Test
    fun `ピットタイミング項目が無効ならバーチャルエナジー予想残り周回数を読み上げない`() {
        val firstLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determinePitTimingVirtualEnergy(
                state = nextLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 160_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(-1, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
    }

    @Test
    fun `給油した周は推定基準から除外され補充直後は再度読み上げる`() {
        val firstLapDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
                settings = settings(),
                observedAtMs = 0L,
            )
        val midLap1Decision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
                settings = settings(),
                observedAtMs = 50_000L,
            )
        val lap2StartDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = midLap1Decision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.85),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val firstWarningDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = lap2StartDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.05),
                settings = settings(),
                observedAtMs = 170_000L,
            )
        // ラップ2の途中で給油。この周は次回以降の推定基準として採用されなくなる。
        val refilledDecision =
            useCase.determinePitTimingVirtualEnergy(
                state =
                    useCase.recordPitTimingAnnounced(
                        firstWarningDecision.state,
                        firstWarningDecision.events.single() as LmuWindowsPitTimingWarning,
                    ),
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
                settings = settings(),
                observedAtMs = 175_000L,
            )

        assertEquals(
            0.1,
            lap2StartDecision.state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption ?: 0.0,
            1e-9,
        )
        assertEquals(
            listOf(LmuWindowsPitTimingWarning(0, source = PitTimingSource.VirtualEnergy)),
            firstWarningDecision.events,
        )
        assertEquals(emptyList<SpeechEvent>(), refilledDecision.events)
        assertEquals(-1, refilledDecision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        assertEquals(true, refilledDecision.state.pitTimingVirtualEnergyTrackingState.currentLapHasRefilled)
    }

    @Test
    fun `ラップ数が戻ったらバーチャルエナジー予想残り周回数の読み上げ履歴をリセットする`() {
        val state =
            LmuWindowsNarratorState(
                lastAnnouncedPitTimingVirtualEnergyLaps = 2,
                lastPitTimingVirtualEnergyEvaluationLap = 5,
                pitTimingVirtualEnergyTrackingState =
                    LmuWindowsPitTimingTrackingState(
                        session = 0,
                        currentLap = 5,
                        currentLapStartedAtMs = 100_000L,
                        currentLapStartValue = 0.4,
                        currentValue = 0.2,
                        refillBaselineValue = 0.2,
                        bestLapTimeMs = 90_000L,
                        observedAtMs = 150_000L,
                    ),
            )

        val decision =
            useCase.determinePitTimingVirtualEnergy(
                state = state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0, session = 0),
                settings = settings(),
                observedAtMs = 200_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(-1, decision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        assertEquals(-1, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
        assertEquals(1, decision.state.pitTimingVirtualEnergyTrackingState.currentLap)
    }

    @Test
    fun `セッションが変わったらラップ数が戻らなくてもバーチャルエナジーの基準値をリセットする`() {
        val qualifyingDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                telemetry = lapTelemetry(currentLap = 0, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.16, session = 5),
                settings = settings(),
                observedAtMs = 0L,
            )

        val raceDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = qualifyingDecision.state,
                telemetry = lapTelemetry(currentLap = 0, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0, session = 10),
                settings = settings(),
                observedAtMs = 10_000L,
            )

        assertEquals(0.16, qualifyingDecision.state.pitTimingVirtualEnergyTrackingState.refillBaselineValue)
        val trackingState = raceDecision.state.pitTimingVirtualEnergyTrackingState
        assertEquals(emptyList<SpeechEvent>(), raceDecision.events)
        assertEquals(10, trackingState.session)
        assertEquals(1.0, trackingState.currentLapStartValue)
        assertEquals(1.0, trackingState.refillBaselineValue)
        assertEquals(-1, raceDecision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        assertEquals(-1, raceDecision.state.lastPitTimingVirtualEnergyEvaluationLap)
    }

    @Test
    fun `閾値未満のバーチャルエナジー残量増加は補充とみなさず消費量の推定に含めない`() {
        val firstDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = LmuWindowsNarratorState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.5),
                settings = settings(),
                observedAtMs = 0L,
            )

        // ジッタによる 0.4% の上振れ（閾値 0.5% 未満）
        val jitterDecision =
            useCase.determinePitTimingVirtualEnergy(
                state = firstDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.504),
                settings = settings(),
                observedAtMs = 1_000L,
            )

        assertEquals(false, jitterDecision.state.pitTimingVirtualEnergyTrackingState.currentLapHasRefilled)
    }

    @Test
    fun `タイヤ摩耗データの車輪が空なら状態を変更せず読み上げない`() {
        val decision =
            useCase.determinePitTimingTyreWear(
                state = LmuWindowsNarratorState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                tyreWear = LmuWindowsTyreWearData(wheels = emptyMap()),
                settings = settings(),
                observedAtMs = 0L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(LmuWindowsNarratorState(), decision.state)
    }

    @Test
    fun `タイヤ摩耗予想残り周回数は最も摩耗した車輪を基準に最速ラップの30秒前を過ぎたら読み上げる`() {
        val firstLapDecision =
            useCase.determinePitTimingTyreWear(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 1.0, fr = 1.0, rl = 1.0, rr = 1.0),
                settings = settings(),
                observedAtMs = 0L,
            )
        val midLap1Decision =
            useCase.determinePitTimingTyreWear(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.9, fr = 1.0, rl = 1.0, rr = 1.0),
                settings = settings(),
                observedAtMs = 45_000L,
            )
        val lapStartDecision =
            useCase.determinePitTimingTyreWear(
                state = midLap1Decision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.8, fr = 1.0, rl = 1.0, rr = 1.0),
                settings = settings(pitTimingTyreWearLapsThreshold = 3),
                observedAtMs = 90_000L,
            )
        val decision =
            useCase.determinePitTimingTyreWear(
                state = lapStartDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.05, fr = 1.0, rl = 1.0, rr = 1.0),
                settings = settings(pitTimingTyreWearLapsThreshold = 3),
                observedAtMs = 150_000L,
            )

        assertEquals(
            listOf(LmuWindowsPitTimingWarning(0, source = PitTimingSource.TyreWear)),
            decision.events,
        )
        assertEquals(2, decision.state.lastPitTimingTyreWearEvaluationLap)
        assertEquals(-1, decision.state.lastAnnouncedPitTimingTyreWearLaps)
        val announcedState =
            useCase.recordPitTimingAnnounced(
                decision.state,
                LmuWindowsPitTimingWarning(0, PitTimingSource.TyreWear),
            )
        assertEquals(0, announcedState.lastAnnouncedPitTimingTyreWearLaps)
    }

    @Test
    fun `ピットタイミング項目が無効ならタイヤ摩耗予想残り周回数を読み上げない`() {
        val firstLapDecision =
            useCase.determinePitTimingTyreWear(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 1.0),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 0L,
            )
        val nextLapDecision =
            useCase.determinePitTimingTyreWear(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.1),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 100_000L,
            )
        val decision =
            useCase.determinePitTimingTyreWear(
                state = nextLapDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.1),
                settings = settings(enabledStates = pitTimingDisabledStates),
                observedAtMs = 160_000L,
            )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(-1, decision.state.lastPitTimingTyreWearEvaluationLap)
    }

    @Test
    fun `タイヤ交換した周は次回以降の推定基準として採用されずタイヤ摩耗の残り周回数の読み上げ履歴をリセットする`() {
        val firstLapDecision =
            useCase.determinePitTimingTyreWear(
                state = pitTimingBoundaryState(),
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
                tyreWear = tyreWear(fl = 1.0),
                settings = settings(),
                observedAtMs = 0L,
            )
        val midLap1Decision =
            useCase.determinePitTimingTyreWear(
                state = firstLapDecision.state,
                telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
                tyreWear = tyreWear(fl = 0.9),
                settings = settings(),
                observedAtMs = 50_000L,
            )
        val lap2StartDecision =
            useCase.determinePitTimingTyreWear(
                state = midLap1Decision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                tyreWear = tyreWear(fl = 0.85),
                settings = settings(),
                observedAtMs = 100_000L,
            )
        val firstWarningDecision =
            useCase.determinePitTimingTyreWear(
                state = lap2StartDecision.state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                tyreWear = tyreWear(fl = 0.05),
                settings = settings(),
                observedAtMs = 170_000L,
            )
        // ラップ2の途中でタイヤ交換。この周は次回以降の推定基準として採用されなくなる。
        val tyreChangedDecision =
            useCase.determinePitTimingTyreWear(
                state =
                    useCase.recordPitTimingAnnounced(
                        firstWarningDecision.state,
                        firstWarningDecision.events.single() as LmuWindowsPitTimingWarning,
                    ),
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                tyreWear = tyreWear(fl = 1.0),
                settings = settings(),
                observedAtMs = 175_000L,
            )

        assertEquals(
            listOf(LmuWindowsPitTimingWarning(0, source = PitTimingSource.TyreWear)),
            firstWarningDecision.events,
        )
        assertEquals(emptyList<SpeechEvent>(), tyreChangedDecision.events)
        assertEquals(-1, tyreChangedDecision.state.lastAnnouncedPitTimingTyreWearLaps)
        assertEquals(true, tyreChangedDecision.state.pitTimingTyreWearTrackingState.currentLapHasRefilled)
        assertEquals(true, tyreChangedDecision.state.pitTimingTyreWearTrackingState.hasRefilled)
        assertEquals(1.0, tyreChangedDecision.state.pitTimingTyreWearTrackingState.refillBaselineValue)
    }

    @Test
    fun `開始時の残量変更を消費量に含めず周回境界からの完走後に推定する`() {
        var state = LmuWindowsNarratorState()
        val samples = listOf(0 to 1.0, 0 to 0.6242384314537048, 1 to 0.6242384314537048, 1 to 0.5856766104698181)
        samples.forEachIndexed { index, (lap, value) ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = lap, bestLapTimeMs = 168_056L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(pitTimingVirtualEnergyLapsThreshold = 5),
                    observedAtMs = index * 168_056L,
                )
            state = decision.state
            assertEquals(null, state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption)
            assertEquals(emptyList<SpeechEvent>(), decision.events)
        }
        val completed =
            useCase.determinePitTimingVirtualEnergy(
                state = state,
                telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 168_056L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.58),
                settings = settings(),
                observedAtMs = 672_224L,
            )
        assertEquals(0.03856182098388672, completed.state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption)
    }

    @Test
    fun `途中参加時のタイヤ摩耗は最初の部分周回を除外して次の完走周を使う`() {
        var state = LmuWindowsNarratorState()
        listOf(7 to 0.9, 7 to 0.8, 8 to 0.79, 8 to 0.78).forEachIndexed { index, (lap, value) ->
            val decision =
                useCase.determinePitTimingTyreWear(
                    state = state,
                    telemetry = lapTelemetry(currentLap = lap, bestLapTimeMs = 90_000L),
                    tyreWear = tyreWear(fl = value),
                    settings = settings(),
                    observedAtMs = index * 90_000L,
                )
            state = decision.state
            assertEquals(null, state.pitTimingTyreWearTrackingState.lastValidLapConsumption)
            assertEquals(emptyList<SpeechEvent>(), decision.events)
        }
        val completed =
            useCase.determinePitTimingTyreWear(
                state = state,
                telemetry = lapTelemetry(currentLap = 9, bestLapTimeMs = 90_000L),
                tyreWear = tyreWear(fl = 0.77),
                settings = settings(),
                observedAtMs = 360_000L,
            )
        assertEquals(0.01, completed.state.pitTimingTyreWearTrackingState.lastValidLapConsumption ?: 0.0, 1e-9)
    }

    @Test
    fun `複数ポーリングの補充を累積検出し補充周を除外して次周に再度読み上げる`() {
        var state = pitTimingBoundaryState()
        // 完走周の消費量0.1を記録し、ラップ2で残り0周を読み上げる。
        listOf(1 to 1.0, 1 to 0.9, 2 to 0.85, 2 to 0.05).forEachIndexed { index, (lap, value) ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = lap, bestLapTimeMs = 100_000L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(),
                    observedAtMs = index * 100_000L,
                )
            state = decision.state
            if (index == 3) {
                val warning = LmuWindowsPitTimingWarning(0, source = PitTimingSource.VirtualEnergy)
                assertEquals(listOf(warning), decision.events)
                state = useCase.recordPitTimingAnnounced(state, warning)
            }
        }
        listOf(0.052, 0.054, 0.056).forEachIndexed { index, value ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(),
                    observedAtMs = 300_016L + index * 16L,
                )
            state = decision.state
            val tracking = state.pitTimingVirtualEnergyTrackingState
            assertEquals(index == 2, tracking.hasRefilled)
            assertEquals(index == 2, tracking.currentLapHasRefilled)
            assertEquals(if (index == 2) value else 0.05, tracking.refillBaselineValue, 1e-9)
            assertEquals(if (index == 2) -1 else 0, state.lastAnnouncedPitTimingVirtualEnergyLaps)
            assertEquals(emptyList<SpeechEvent>(), decision.events)
        }
        val nextLap =
            useCase.determinePitTimingVirtualEnergy(
                state = state,
                telemetry = lapTelemetry(currentLap = 3, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.05),
                settings = settings(),
                observedAtMs = 400_000L,
            )
        assertEquals(0.1, nextLap.state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption ?: 0.0, 1e-9)
        assertEquals(0.05, nextLap.state.pitTimingVirtualEnergyTrackingState.refillBaselineValue, 1e-9)
        val warning =
            useCase.determinePitTimingVirtualEnergy(
                state = nextLap.state,
                telemetry = lapTelemetry(currentLap = 3, bestLapTimeMs = 100_000L),
                virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.04),
                settings = settings(),
                observedAtMs = 470_000L,
            )
        assertEquals(
            listOf(LmuWindowsPitTimingWarning(0, source = PitTimingSource.VirtualEnergy)),
            warning.events,
        )
    }

    @Test
    fun `閾値未満の単発ジッタは補充とみなさず消費時に基準値を更新する`() {
        var state = pitTimingBoundaryState()
        listOf(0.8, 0.802, 0.79).forEachIndexed { index, value ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(),
                    observedAtMs = index * 16L,
                )
            state = decision.state
            val tracking = state.pitTimingVirtualEnergyTrackingState
            assertEquals(false, tracking.hasRefilled)
            assertEquals(false, tracking.currentLapHasRefilled)
            assertEquals(if (index == 2) 0.79 else 0.8, tracking.refillBaselineValue, 1e-9)
        }
    }

    @Test
    fun `周回番号が飛んだ区間とその直後の周は消費量に採用しない`() {
        var state = pitTimingBoundaryState()
        listOf(1 to 0.9, 3 to 0.7, 4 to 0.6).forEachIndexed { index, (lap, value) ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = lap, bestLapTimeMs = 90_000L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(),
                    observedAtMs = index * 90_000L,
                )
            state = decision.state
            assertEquals(null, state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption)
        }
    }

    @Test
    fun `周回境界で給油を検出したら前周と次周を推定から除外する`() {
        var state = pitTimingBoundaryState()
        listOf(1 to 0.8, 1 to 0.7, 2 to 0.95, 2 to 0.9, 3 to 0.85).forEachIndexed { index, (lap, value) ->
            val decision =
                useCase.determinePitTimingVirtualEnergy(
                    state = state,
                    telemetry = lapTelemetry(currentLap = lap, bestLapTimeMs = 90_000L),
                    virtualEnergy = remainingVirtualEnergy(remainingRatio = value),
                    settings = settings(),
                    observedAtMs = index * 45_000L,
                )
            state = decision.state
            assertEquals(null, state.pitTimingVirtualEnergyTrackingState.lastValidLapConsumption)
        }
    }
}

private fun pitTimingBoundaryState(): LmuWindowsNarratorState =
    LmuWindowsNarratorState(
        pitTimingVirtualEnergyTrackingState =
            LmuWindowsPitTimingTrackingState(
                session = 0,
                currentLap = 0,
                currentValue = 1.0,
                refillBaselineValue = 1.0,
                currentLapStartValue = 1.0,
            ),
        pitTimingTyreWearTrackingState =
            LmuWindowsPitTimingTrackingState(
                currentLap = 0,
                currentValue = 1.0,
                refillBaselineValue = 1.0,
                currentLapStartValue = 1.0,
            ),
    )

private val allEnabledStates: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.MyBestLap.Root to true,
        LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled to true,
        LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
        LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
        LmuWindowsReadoutItemKey.VehicleApproach.Sustained to true,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
        LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true,
        LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to true,
        LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to true,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
        LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to true,
        LmuWindowsReadoutItemKey.TyreWear.Root to true,
        LmuWindowsReadoutItemKey.TyreWear.WarningReadout to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout to true,
        LmuWindowsReadoutItemKey.BrakeWear.Root to true,
        LmuWindowsReadoutItemKey.BrakeWear.WarningReadout to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout to true,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.Flag.Root to true,
        LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
        LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
        LmuWindowsReadoutItemKey.Flag.RedFlag to true,
    )

private val pitTimingDisabledStates: Map<ReadoutItemKey, Boolean> =
    allEnabledStates + mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to false)

@Suppress("LongParameterList")
private fun settings(
    enabledStates: Map<ReadoutItemKey, Boolean> = allEnabledStates,
    currentLap: Int = 1,
    skipFirstLap: Boolean = false,
    sustainedApproachDurationSeconds: Int = 7,
    tyreTemperatureHighThresholdCelsius: Int = 90,
    tyreWearThresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
    brakeTemperatureHighThresholdCelsius: Int = 700,
    brakeWearLowThresholdPercent: Int = 20,
    remainingVirtualEnergyThresholdPercentage: Int = 30,
    pitTimingVirtualEnergyLapsThreshold: Int = 3,
    pitTimingTyreWearLapsThreshold: Int = 3,
) = LmuWindowsNarratorReadoutSettings(
    enabledStates = enabledStates,
    currentLap = currentLap,
    skipFirstLap = skipFirstLap,
    vehicleApproachSustainedApproachDurationSeconds = sustainedApproachDurationSeconds,
    tyreTemperatureHighThresholdCelsius = Celsius(tyreTemperatureHighThresholdCelsius),
    tyreTemperatureLowWarningPhases =
        setOf(
            SessionPhase.GARAGE,
            SessionPhase.WARM_UP,
            SessionPhase.GRID_WALK,
            SessionPhase.FORMATION,
        ),
    tyreWearThresholdPercentage = tyreWearThresholdPercentage,
    brakeTemperatureHighThresholdCelsius = Celsius(brakeTemperatureHighThresholdCelsius),
    brakeWearLowThresholdPercent = brakeWearLowThresholdPercent,
    remainingVirtualEnergyThresholdPercentage = remainingVirtualEnergyThresholdPercentage,
    pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold,
    pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold,
)

private fun telemetry(bestLapTimeMs: Long) =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = bestLapTimeMs,
                sector1Ms = 0L,
                sector1And2Ms = 0L,
                currentLap = 0,
                maxLaps = 0,
            ),
        vehicle =
            LmuWindowsVehicleData(
                localVelocityX = 0.0,
                localVelocityY = 0.0,
                localVelocityZ = 0.0,
                positionX = 0.0,
                positionY = 0.0,
                positionZ = 0.0,
            ),
    )

private fun lapTelemetry(
    currentLap: Int,
    bestLapTimeMs: Long,
) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
    timing =
        LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = bestLapTimeMs,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
    vehicle =
        LmuWindowsVehicleData(
            localVelocityX = 0.0,
            localVelocityY = 0.0,
            localVelocityZ = 0.0,
            positionX = 0.0,
            positionY = 0.0,
            positionZ = 0.0,
        ),
)

private fun leftVehicleApproach(vehicleId: Int) =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(vehicleId),
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = LateralDistanceMeters(3.0),
        lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
    )

private fun rightVehicleApproach(vehicleId: Int) =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = emptySet(),
        sideBySideRightVehicleIds = setOf(vehicleId),
        lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
        lateralDistanceRightMeters = LateralDistanceMeters(3.0),
    )

private fun leftAndRightVehicleApproach() =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(1),
        sideBySideRightVehicleIds = setOf(2),
        lateralDistanceLeftMeters = LateralDistanceMeters(3.0),
        lateralDistanceRightMeters = LateralDistanceMeters(3.0),
    )

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
    sectorFlags: List<SectorFlagState> = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = sectorFlags,
    playerFlag = playerFlag,
    playerUnderYellow = false,
)

private fun damage(
    overheating: Boolean = false,
    partDetached: Boolean = false,
) = LmuWindowsVehicleDamageData(
    overheating = overheating,
    partDetached = partDetached,
    lastImpactMagnitude = 0.0,
)

private fun tyreDetached(vararg detachedWheels: WheelIndex) =
    LmuWindowsTyreDetachedData(
        wheels = WheelIndex.entries.associateWith { it in detachedWheels },
    )

private fun tyreTemperatureInput(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
    raceFlags: LmuWindowsRaceFlagsData = clearFlags(),
) = TyreTemperatureReadoutInput(
    tyreCarcassTemperature = tyreTemperature(fl, fr, rl, rr),
    raceFlags = raceFlags,
)

private fun tyreTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsTyreCarcassTemperatureData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to CelsiusReading(fl.toFloat()),
            WheelIndex.FRONT_RIGHT to CelsiusReading(fr.toFloat()),
            WheelIndex.REAR_LEFT to CelsiusReading(rl.toFloat()),
            WheelIndex.REAR_RIGHT to CelsiusReading(rr.toFloat()),
        ),
)

private fun tyreWear(
    fl: Double = 1.0,
    fr: Double = 1.0,
    rl: Double = 1.0,
    rr: Double = 1.0,
) = LmuWindowsTyreWearData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(fl),
            WheelIndex.FRONT_RIGHT to LmuWindowsTyreWearRatio(fr),
            WheelIndex.REAR_LEFT to LmuWindowsTyreWearRatio(rl),
            WheelIndex.REAR_RIGHT to LmuWindowsTyreWearRatio(rr),
        ),
)

private fun brakeTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsBrakeTemperatureData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to CelsiusReading(fl.toFloat()),
            WheelIndex.FRONT_RIGHT to CelsiusReading(fr.toFloat()),
            WheelIndex.REAR_LEFT to CelsiusReading(rl.toFloat()),
            WheelIndex.REAR_RIGHT to CelsiusReading(rr.toFloat()),
        ),
)

private fun remainingVirtualEnergy(
    remainingRatio: Double = 1.0,
    session: Int = 0,
) = LmuWindowsVirtualEnergyData(
    remainingRatio = LmuWindowsVirtualEnergyRatio(remainingRatio),
    session = session,
)

private fun brakeWear(
    fl: Int = 80,
    fr: Int = 80,
    rl: Int = 80,
    rr: Int = 80,
) = LmuWindowsBrakeWearRemainingData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), fl.toFloat()),
            WheelIndex.FRONT_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), fr.toFloat()),
            WheelIndex.REAR_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), rl.toFloat()),
            WheelIndex.REAR_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), rr.toFloat()),
        ),
)
