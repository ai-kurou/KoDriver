package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("TooManyFunctions")
class DetermineLmuWindowsNarratorReadoutUseCaseTest {
    private val useCase = DetermineLmuWindowsNarratorReadoutUseCase()

    @Test
    fun `初回の自己ベストラップは状態だけ更新する`() {
        val decision = useCase.determineMyBestLap(
            state = LmuWindowsNarratorState(),
            telemetry = telemetry(bestLapTimeMs = 60_000L),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(60_000L, decision.state.previousBestLapTimeMs)
    }

    @Test
    fun `自己ベストラップが更新されると設定した音声イベントを返す`() {
        val first = useCase.determineMyBestLap(
            state = LmuWindowsNarratorState(),
            telemetry = telemetry(bestLapTimeMs = 60_000L),
            settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
        )

        val second = useCase.determineMyBestLap(
            state = first.state,
            telemetry = telemetry(bestLapTimeMs = 59_000L),
            settings = settings(myBestLapVoiceType = MyBestLapVoiceType.CASUAL),
        )

        assertEquals(listOf(SpeechEvent.LmuWindowsMyBestLapCasual), second.events)
        assertEquals(59_000L, second.state.personalBestMs)
    }

    @Test
    fun `自己ベストラップ項目が無効なら読み上げない`() {
        val first = useCase.determineMyBestLap(
            state = LmuWindowsNarratorState(),
            telemetry = telemetry(bestLapTimeMs = 60_000L),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to false),
            ),
        )

        val second = useCase.determineMyBestLap(
            state = first.state,
            telemetry = telemetry(bestLapTimeMs = 59_000L),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左接近が50ms継続するとCarLeftを返す`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), first.events)
        assertEquals(listOf(SpeechEvent.CarLeft), second.events)
    }

    @Test
    fun `右接近の読み上げ種別を変更するとRightApproachを返す`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = rightVehicleApproach(vehicleId = 1),
            settings = settings(startReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = rightVehicleApproach(vehicleId = 1),
            settings = settings(startReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH),
            observedAtMs = 50L,
        )

        assertEquals(listOf(SpeechEvent.RightApproach), second.events)
    }

    @Test
    fun `50ms未満の接近では読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(),
            observedAtMs = 49L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左右同時接近は読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftAndRightVehicleApproach(),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftAndRightVehicleApproach(),
            settings = settings(),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `接近読み上げ無効時は状態だけ更新する`() {
        val decision = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to false,
                ),
            ),
            observedAtMs = 0L,
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertNotNull(decision.state.vehicleApproachState.left[1])
    }

    @Test
    fun `1周目スキップ中の0周目は接近を読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(skipFirstLap = true, currentLap = 0),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(skipFirstLap = true, currentLap = 0),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `車両接近項目が無効なら接近を読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            ),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            ),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左接近が閾値秒数継続するとKeepRightを返す`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 7_000L,
        )

        assertEquals(listOf(SpeechEvent.CarLeft, SpeechEvent.KeepRight), second.events)
    }

    @Test
    fun `右接近の継続読み上げ種別を変更するとLeftSustainedを返す`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = rightVehicleApproach(vehicleId = 1),
            settings = settings(
                sustainedApproachDurationSeconds = 7,
                sustainedReadoutType = VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
            ),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = rightVehicleApproach(vehicleId = 1),
            settings = settings(
                sustainedApproachDurationSeconds = 7,
                sustainedReadoutType = VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
            ),
            observedAtMs = 7_000L,
        )

        assertEquals(listOf(SpeechEvent.CarRight, SpeechEvent.LeftSustained), second.events)
    }

    @Test
    fun `閾値秒数未満では継続接近を読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 6_999L,
        )

        assertEquals(listOf(SpeechEvent.CarLeft), second.events)
    }

    @Test
    fun `接近継続時の読み上げが無効なら継続接近を読み上げない`() {
        val disabledStates = allEnabledStates + mapOf(
            ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false,
        )
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
            observedAtMs = 7_000L,
        )

        assertEquals(listOf(SpeechEvent.CarLeft), second.events)
    }

    @Test
    fun `車両接近項目が無効なら継続接近も読み上げない`() {
        val disabledStates = allEnabledStates + mapOf(
            ReadoutItemKey.LmuWindows.VehicleApproach.Root to false,
        )
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(enabledStates = disabledStates, sustainedApproachDurationSeconds = 7),
            observedAtMs = 7_000L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `一度読み上げた継続接近は同じ側の接近が続いても再度読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 0L,
        )
        val second = useCase.determineVehicleApproach(
            state = first.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 7_000L,
        )

        val third = useCase.determineVehicleApproach(
            state = second.state,
            vehicleApproach = leftVehicleApproach(vehicleId = 1),
            settings = settings(sustainedApproachDurationSeconds = 7),
            observedAtMs = 14_000L,
        )

        assertEquals(emptyList<SpeechEvent>(), third.events)
    }

    @Test
    fun `初回の旗情報は状態だけ更新する`() {
        val decision = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(playerFlag = PrimaryFlag.BLUE),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(PrimaryFlag.BLUE, decision.state.previousRaceFlags?.playerFlag)
    }

    @Test
    fun `旗の変化を読み上げイベントに変換する`() {
        val first = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(),
            settings = settings(),
        )

        val second = useCase.determineRaceFlags(
            state = first.state,
            raceFlags = clearFlags(
                gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                playerFlag = PrimaryFlag.BLUE,
                sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
            ),
            settings = settings(),
        )

        assertEquals(
            listOf(SpeechEvent.BlueFlag, SpeechEvent.YellowFlag, SpeechEvent.FullCourseYellow),
            second.events,
        )
    }

    @Test
    fun `赤旗の変化をSessionStopに変換する`() {
        val first = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(),
            settings = settings(),
        )

        val second = useCase.determineRaceFlags(
            state = first.state,
            raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
            settings = settings(),
        )

        assertEquals(listOf(SpeechEvent.SessionStop), second.events)
    }

    @Test
    fun `赤旗の音声種別がRED_FLAGのときはRedFlagイベントに変換する`() {
        val first = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(),
            settings = settings(redFlagVoiceType = RedFlagVoiceType.RED_FLAG),
        )

        val second = useCase.determineRaceFlags(
            state = first.state,
            raceFlags = clearFlags(gamePhase = SessionPhase.RED_FLAG),
            settings = settings(redFlagVoiceType = RedFlagVoiceType.RED_FLAG),
        )

        assertEquals(listOf(SpeechEvent.RedFlag), second.events)
    }

    @Test
    fun `無効な旗項目は読み上げない`() {
        val first = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(),
            settings = settings(),
        )

        val second = useCase.determineRaceFlags(
            state = first.state,
            raceFlags = clearFlags(
                gamePhase = SessionPhase.RED_FLAG,
                playerFlag = PrimaryFlag.BLUE,
                sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            ),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.Flag.BlueFlag to false,
                    ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to false,
                    ReadoutItemKey.LmuWindows.Flag.RedFlag to false,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `フラッグ項目が無効なら詳細フラッグ項目が有効でも読み上げない`() {
        val first = useCase.determineRaceFlags(
            state = LmuWindowsNarratorState(),
            raceFlags = clearFlags(),
            settings = settings(),
        )

        val second = useCase.determineRaceFlags(
            state = first.state,
            raceFlags = clearFlags(
                gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                playerFlag = PrimaryFlag.BLUE,
                sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            ),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.Flag.Root to false,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
        assertEquals(SessionPhase.FULL_COURSE_YELLOW, second.state.previousRaceFlags?.gamePhase)
    }

    @Test
    fun `初回の車両故障情報は状態だけ更新する`() {
        val decision = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(),
            vehicleDamage = damage(overheating = true),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.previousVehicleDamage?.overheating)
    }

    @Test
    fun `オーバーヒートがfalseからtrueに変化するとOverheatingを返す`() {
        val first = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(),
            vehicleDamage = damage(overheating = false),
            settings = settings(),
        )

        val second = useCase.determineVehicleDamage(
            state = first.state,
            vehicleDamage = damage(overheating = true),
            settings = settings(),
        )

        assertEquals(listOf(SpeechEvent.Overheating), second.events)
    }

    @Test
    fun `オーバーヒートが継続しても再度読み上げない`() {
        val first = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = true)),
            vehicleDamage = damage(overheating = true),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), first.events)
    }

    @Test
    fun `オーバーヒート項目が無効なら読み上げない`() {
        val decision = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
            vehicleDamage = damage(overheating = true),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `車両故障項目が無効なら読み上げない`() {
        val decision = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
            vehicleDamage = damage(overheating = true),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `いずれかのタイヤが閾値以上になると TyreOverheat を返す`() {
        val decision = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(listOf(SpeechEvent.TyreOverheat), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `高温状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(tyreOverheating = true)
        val decision = useCase.determineTyreTemperatureOverheat(
            state = state,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() {
        val overheatState = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val cooledState = useCase.determineTyreTemperatureOverheat(
            state = overheatState,
            data = tyreTemperature(fl = 85.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val reovertState = useCase.determineTyreTemperatureOverheat(
            state = cooledState,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(false, cooledState.tyreOverheating)
        assertEquals(listOf(SpeechEvent.TyreOverheat), reovertState.events)
    }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() {
        val decision = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `無効中に過熱した状態で再有効化しても読み上げない`() {
        val disabledState = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
            ),
        ).state

        val reenabledDecision = useCase.determineTyreTemperatureOverheat(
            state = disabledState,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(emptyList<SpeechEvent>(), reenabledDecision.events)
    }

    @Test
    fun `過熱警告スイッチがOFFの場合は読み上げられない`() {
        val decision = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `いずれかのタイヤの摩耗率が閾値以上になると TyreWearWarning を返す`() {
        val decision = useCase.determineTyreWear(
            state = LmuWindowsNarratorState(),
            data = tyreWear(fl = 0.4),
            settings = settings(tyreWearThresholdPercentage = 50),
        )

        assertEquals(listOf(SpeechEvent.TyreWearWarning), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `摩耗警告状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(tyreWearWarned = true)
        val decision = useCase.determineTyreWear(
            state = state,
            data = tyreWear(fl = 0.4),
            settings = settings(tyreWearThresholdPercentage = 50),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `全タイヤが閾値未満に戻ると再度読み上げ可能になる`() {
        val warnedState = useCase.determineTyreWear(
            state = LmuWindowsNarratorState(),
            data = tyreWear(fl = 0.4),
            settings = settings(tyreWearThresholdPercentage = 50),
        ).state

        val recoveredState = useCase.determineTyreWear(
            state = warnedState,
            data = tyreWear(fl = 0.6),
            settings = settings(tyreWearThresholdPercentage = 50),
        ).state

        val rewarnedDecision = useCase.determineTyreWear(
            state = recoveredState,
            data = tyreWear(fl = 0.4),
            settings = settings(tyreWearThresholdPercentage = 50),
        )

        assertEquals(false, recoveredState.tyreWearWarned)
        assertEquals(listOf(SpeechEvent.TyreWearWarning), rewarnedDecision.events)
    }

    @Test
    fun `タイヤ摩耗項目が無効なら読み上げない`() {
        val decision = useCase.determineTyreWear(
            state = LmuWindowsNarratorState(),
            data = tyreWear(fl = 0.4),
            settings = settings(
                tyreWearThresholdPercentage = 50,
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.TyreWear.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreWearWarned)
    }

    @Test
    fun `残量が閾値以下になると RemainingVirtualEnergyWarning を返す`() {
        val decision = useCase.determineRemainingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            data = remainingVirtualEnergy(remainingRatio = 0.4),
            settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
        )

        assertEquals(listOf(SpeechEvent.RemainingVirtualEnergyWarning), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `残量警告状態が継続しても再度読み上げない`() {
        val state = LmuWindowsNarratorState(remainingVirtualEnergyWarned = true)
        val decision = useCase.determineRemainingVirtualEnergy(
            state = state,
            data = remainingVirtualEnergy(remainingRatio = 0.4),
            settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `残量が閾値より上に戻ると再度読み上げ可能になる`() {
        val warnedState = useCase.determineRemainingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            data = remainingVirtualEnergy(remainingRatio = 0.4),
            settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
        ).state

        val recoveredState = useCase.determineRemainingVirtualEnergy(
            state = warnedState,
            data = remainingVirtualEnergy(remainingRatio = 0.6),
            settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
        ).state

        val rewarnedDecision = useCase.determineRemainingVirtualEnergy(
            state = recoveredState,
            data = remainingVirtualEnergy(remainingRatio = 0.4),
            settings = settings(remainingVirtualEnergyThresholdPercentage = 50),
        )

        assertEquals(false, recoveredState.remainingVirtualEnergyWarned)
        assertEquals(listOf(SpeechEvent.RemainingVirtualEnergyWarning), rewarnedDecision.events)
    }

    @Test
    fun `バーチャルエナジー残量項目が無効なら読み上げない`() {
        val decision = useCase.determineRemainingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            data = remainingVirtualEnergy(remainingRatio = 0.4),
            settings = settings(
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledStates = allEnabledStates +
                    mapOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.remainingVirtualEnergyWarned)
    }

    @Test
    fun `タイヤ温度項目が無効なら過熱警告スイッチがONでも読み上げない`() {
        val decision = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                    ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `閾値ちょうどは高温扱い`() {
        val decision = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 90.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(listOf(SpeechEvent.TyreOverheat), decision.events)
    }

    @Test
    fun `ヒステリシス範囲内に下がっただけでは過熱状態を維持し再度読み上げない`() {
        val overheatState = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val bandState = useCase.determineTyreTemperatureOverheat(
            state = overheatState,
            data = tyreTemperature(fl = 87.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        val reheatedDecision = useCase.determineTyreTemperatureOverheat(
            state = bandState.state,
            data = tyreTemperature(fl = 91.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(true, bandState.state.tyreOverheating)
        assertEquals(emptyList<SpeechEvent>(), bandState.events)
        assertEquals(emptyList<SpeechEvent>(), reheatedDecision.events)
    }

    @Test
    fun `ヒステリシス下限まで下がると再度読み上げ可能になる`() {
        val overheatState = useCase.determineTyreTemperatureOverheat(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val cooledState = useCase.determineTyreTemperatureOverheat(
            state = overheatState,
            data = tyreTemperature(fl = 85.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(false, cooledState.state.tyreOverheating)
    }

    @Test
    fun `初回のgamePhase観測では低温でも読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(SessionPhase.GARAGE, decision.state.previousGamePhaseForTyreLowWarning)
    }

    @Test
    fun `ガレージに遷移した瞬間に低温タイヤがあるとTyreColdを返す`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings(),
        )

        assertEquals(listOf(SpeechEvent.TyreCold), decision.events)
    }

    @Test
    fun `対象外のgamePhaseに遷移しても読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.COUNTDOWN),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `gamePhaseが変化しなければ低温でも読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GARAGE),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `1輪でも60度以下なら読み上げる`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 60.0, fr = 80.0, rl = 80.0, rr = 80.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.WARM_UP),
            settings = settings(),
        )

        assertEquals(listOf(SpeechEvent.TyreCold), decision.events)
    }

    @Test
    fun `全タイヤが60度超なら読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 61.0, fr = 80.0, rl = 80.0, rr = 80.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.WARM_UP),
            settings = settings(),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `選択解除されたgamePhaseに遷移しても読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings().copy(
                tyreTemperatureLowWarningPhases = setOf(
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
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to false,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `タイヤ温度項目が無効なら低温警告スイッチがONでも読み上げない`() {
        val decision = useCase.determineTyreTemperatureLow(
            state = LmuWindowsNarratorState(previousGamePhaseForTyreLowWarning = SessionPhase.GREEN_FLAG),
            data = tyreTemperature(fl = 55.0),
            raceFlags = clearFlags(gamePhase = SessionPhase.GARAGE),
            settings = settings(
                enabledStates = allEnabledStates + mapOf(
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                    ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `バーチャルエナジー予想残り周回数は直近に完走したラップの消費率を使って最速ラップの30秒前を過ぎたら読み上げる`() {
        val firstLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
            settings = settings(),
            observedAtMs = 0L,
        )
        val midLap1Decision = useCase.determinePitTimingVirtualEnergy(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
            settings = settings(),
            observedAtMs = 45_000L,
        )
        // ラップ1完走時の消費率(0.1)が、ラップ2開始時に次回以降の推定基準として採用される。
        val lapStartDecision = useCase.determinePitTimingVirtualEnergy(
            state = midLap1Decision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.8),
            settings = settings(pitTimingVirtualEnergyLapsThreshold = 3),
            observedAtMs = 90_000L,
        )
        val decision = useCase.determinePitTimingVirtualEnergy(
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
        assertEquals(listOf(SpeechEvent.PitTimingVirtualEnergyWarning(0)), decision.events)
        assertEquals(2, decision.state.lastPitTimingVirtualEnergyEvaluationLap)
        assertEquals(0, decision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
    }

    @Test
    fun `バーチャルエナジー予想残り周回数は読み上げタイミング前なら読み上げない`() {
        val firstLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
            settings = settings(),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
            settings = settings(),
            observedAtMs = 100_000L,
        )
        val decision = useCase.determinePitTimingVirtualEnergy(
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
        val firstLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
            settings = settings(enabledStates = pitTimingDisabledStates),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.1),
            settings = settings(enabledStates = pitTimingDisabledStates),
            observedAtMs = 100_000L,
        )
        val decision = useCase.determinePitTimingVirtualEnergy(
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
        val firstLapDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0),
            settings = settings(),
            observedAtMs = 0L,
        )
        val midLap1Decision = useCase.determinePitTimingVirtualEnergy(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.9),
            settings = settings(),
            observedAtMs = 50_000L,
        )
        val lap2StartDecision = useCase.determinePitTimingVirtualEnergy(
            state = midLap1Decision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.85),
            settings = settings(),
            observedAtMs = 100_000L,
        )
        val firstWarningDecision = useCase.determinePitTimingVirtualEnergy(
            state = lap2StartDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.05),
            settings = settings(),
            observedAtMs = 170_000L,
        )
        // ラップ2の途中で給油。この周は次回以降の推定基準として採用されなくなる。
        val refilledDecision = useCase.determinePitTimingVirtualEnergy(
            state = firstWarningDecision.state,
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
        assertEquals(listOf(SpeechEvent.PitTimingVirtualEnergyWarning(0)), firstWarningDecision.events)
        assertEquals(emptyList<SpeechEvent>(), refilledDecision.events)
        assertEquals(-1, refilledDecision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        assertEquals(true, refilledDecision.state.pitTimingVirtualEnergyTrackingState.currentLapHasRefilled)
    }

    @Test
    fun `ラップ数が戻ったらバーチャルエナジー予想残り周回数の読み上げ履歴をリセットする`() {
        val state = LmuWindowsNarratorState(
            lastAnnouncedPitTimingVirtualEnergyLaps = 2,
            lastPitTimingVirtualEnergyEvaluationLap = 5,
            pitTimingVirtualEnergyTrackingState = LmuWindowsPitTimingTrackingState(
                session = 0,
                currentLap = 5,
                currentLapStartedAtMs = 100_000L,
                currentLapStartValue = 0.4,
                currentValue = 0.2,
                bestLapTimeMs = 90_000L,
                observedAtMs = 150_000L,
            ),
        )

        val decision = useCase.determinePitTimingVirtualEnergy(
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
        val qualifyingDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 0, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.16, session = 5),
            settings = settings(),
            observedAtMs = 0L,
        )

        val raceDecision = useCase.determinePitTimingVirtualEnergy(
            state = qualifyingDecision.state,
            telemetry = lapTelemetry(currentLap = 0, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 1.0, session = 10),
            settings = settings(),
            observedAtMs = 10_000L,
        )

        val trackingState = raceDecision.state.pitTimingVirtualEnergyTrackingState
        assertEquals(emptyList<SpeechEvent>(), raceDecision.events)
        assertEquals(10, trackingState.session)
        assertEquals(1.0, trackingState.currentLapStartValue)
        assertEquals(-1, raceDecision.state.lastAnnouncedPitTimingVirtualEnergyLaps)
        assertEquals(-1, raceDecision.state.lastPitTimingVirtualEnergyEvaluationLap)
    }

    @Test
    fun `しきい値未満のバーチャルエナジー残量増加は補充とみなさず消費量の推定に含めない`() {
        val firstDecision = useCase.determinePitTimingVirtualEnergy(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.5),
            settings = settings(),
            observedAtMs = 0L,
        )

        // ジッタによる 0.4% の上振れ（しきい値 0.5% 未満）
        val jitterDecision = useCase.determinePitTimingVirtualEnergy(
            state = firstDecision.state,
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            virtualEnergy = remainingVirtualEnergy(remainingRatio = 0.504),
            settings = settings(),
            observedAtMs = 1_000L,
        )

        assertEquals(false, jitterDecision.state.pitTimingVirtualEnergyTrackingState.currentLapHasRefilled)
    }

    @Test
    fun `タイヤ摩耗予想残り周回数は最も摩耗した車輪を基準に最速ラップの30秒前を過ぎたら読み上げる`() {
        val firstLapDecision = useCase.determinePitTimingTyreWear(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 1.0, fr = 1.0, rl = 1.0, rr = 1.0),
            settings = settings(),
            observedAtMs = 0L,
        )
        val midLap1Decision = useCase.determinePitTimingTyreWear(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 0.9, fr = 1.0, rl = 1.0, rr = 1.0),
            settings = settings(),
            observedAtMs = 45_000L,
        )
        val lapStartDecision = useCase.determinePitTimingTyreWear(
            state = midLap1Decision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 0.8, fr = 1.0, rl = 1.0, rr = 1.0),
            settings = settings(pitTimingTyreWearLapsThreshold = 3),
            observedAtMs = 90_000L,
        )
        val decision = useCase.determinePitTimingTyreWear(
            state = lapStartDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 0.05, fr = 1.0, rl = 1.0, rr = 1.0),
            settings = settings(pitTimingTyreWearLapsThreshold = 3),
            observedAtMs = 150_000L,
        )

        assertEquals(listOf(SpeechEvent.PitTimingTyreWearWarning(0)), decision.events)
        assertEquals(2, decision.state.lastPitTimingTyreWearEvaluationLap)
        assertEquals(0, decision.state.lastAnnouncedPitTimingTyreWearLaps)
    }

    @Test
    fun `ピットタイミング項目が無効ならタイヤ摩耗予想残り周回数を読み上げない`() {
        val firstLapDecision = useCase.determinePitTimingTyreWear(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 1.0),
            settings = settings(enabledStates = pitTimingDisabledStates),
            observedAtMs = 0L,
        )
        val nextLapDecision = useCase.determinePitTimingTyreWear(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 90_000L),
            tyreWear = tyreWear(fl = 0.1),
            settings = settings(enabledStates = pitTimingDisabledStates),
            observedAtMs = 100_000L,
        )
        val decision = useCase.determinePitTimingTyreWear(
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
        val firstLapDecision = useCase.determinePitTimingTyreWear(
            state = LmuWindowsNarratorState(),
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
            tyreWear = tyreWear(fl = 1.0),
            settings = settings(),
            observedAtMs = 0L,
        )
        val midLap1Decision = useCase.determinePitTimingTyreWear(
            state = firstLapDecision.state,
            telemetry = lapTelemetry(currentLap = 1, bestLapTimeMs = 100_000L),
            tyreWear = tyreWear(fl = 0.9),
            settings = settings(),
            observedAtMs = 50_000L,
        )
        val lap2StartDecision = useCase.determinePitTimingTyreWear(
            state = midLap1Decision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
            tyreWear = tyreWear(fl = 0.85),
            settings = settings(),
            observedAtMs = 100_000L,
        )
        val firstWarningDecision = useCase.determinePitTimingTyreWear(
            state = lap2StartDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
            tyreWear = tyreWear(fl = 0.05),
            settings = settings(),
            observedAtMs = 170_000L,
        )
        // ラップ2の途中でタイヤ交換。この周は次回以降の推定基準として採用されなくなる。
        val tyreChangedDecision = useCase.determinePitTimingTyreWear(
            state = firstWarningDecision.state,
            telemetry = lapTelemetry(currentLap = 2, bestLapTimeMs = 100_000L),
            tyreWear = tyreWear(fl = 1.0),
            settings = settings(),
            observedAtMs = 175_000L,
        )

        assertEquals(listOf(SpeechEvent.PitTimingTyreWearWarning(0)), firstWarningDecision.events)
        assertEquals(emptyList<SpeechEvent>(), tyreChangedDecision.events)
        assertEquals(-1, tyreChangedDecision.state.lastAnnouncedPitTimingTyreWearLaps)
        assertEquals(true, tyreChangedDecision.state.pitTimingTyreWearTrackingState.currentLapHasRefilled)
    }
}

private val allEnabledStates: Map<ReadoutItemKey, Boolean> = mapOf(
    ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
    ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true,
    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to true,
    ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
    ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true,
    ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
    ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
    ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
    ReadoutItemKey.LmuWindows.TyreWear.Root to true,
    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
    ReadoutItemKey.LmuWindows.PitTiming.Root to true,
    ReadoutItemKey.LmuWindows.Flag.Root to true,
    ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
    ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
    ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
    ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
)

private val pitTimingDisabledStates: Map<ReadoutItemKey, Boolean> =
    allEnabledStates + mapOf(ReadoutItemKey.LmuWindows.PitTiming.Root to false)

@Suppress("LongParameterList")
private fun settings(
    enabledStates: Map<ReadoutItemKey, Boolean> = allEnabledStates,
    myBestLapVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
    redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
    currentLap: Int = 1,
    skipFirstLap: Boolean = false,
    startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
    sustainedApproachDurationSeconds: Int = 7,
    sustainedReadoutType: VehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
    tyreTemperatureHighThresholdCelsius: Int = 90,
    tyreWearThresholdPercentage: Int = 50,
    remainingVirtualEnergyThresholdPercentage: Int = 50,
    pitTimingVirtualEnergyLapsThreshold: Int = 3,
    pitTimingTyreWearLapsThreshold: Int = 3,
) = LmuWindowsNarratorReadoutSettings(
    enabledStates = enabledStates,
    myBestLapVoiceType = myBestLapVoiceType,
    redFlagVoiceType = redFlagVoiceType,
    currentLap = currentLap,
    skipFirstLap = skipFirstLap,
    vehicleApproachStartReadoutType = startReadoutType,
    vehicleApproachSustainedApproachDurationSeconds = sustainedApproachDurationSeconds,
    vehicleApproachSustainedReadoutType = sustainedReadoutType,
    tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThresholdCelsius,
    tyreTemperatureLowWarningPhases = setOf(
        SessionPhase.GARAGE,
        SessionPhase.WARM_UP,
        SessionPhase.GRID_WALK,
        SessionPhase.FORMATION,
    ),
    tyreWearThresholdPercentage = tyreWearThresholdPercentage,
    remainingVirtualEnergyThresholdPercentage = remainingVirtualEnergyThresholdPercentage,
    pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold,
    pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold,
)

private fun telemetry(bestLapTimeMs: Long) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 0L,
        lastLapTimeMs = 0L,
        bestLapTimeMs = bestLapTimeMs,
        sector1Ms = 0L,
        sector1And2Ms = 0L,
        currentLap = 0,
        maxLaps = 0,
    ),
    vehicle = LmuWindowsVehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    ),
)

private fun lapTelemetry(currentLap: Int, bestLapTimeMs: Long) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 0L,
        lastLapTimeMs = 0L,
        bestLapTimeMs = bestLapTimeMs,
        sector1Ms = 0L,
        sector1And2Ms = 0L,
        currentLap = currentLap,
        maxLaps = 0,
    ),
    vehicle = LmuWindowsVehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    ),
)

private fun leftVehicleApproach(vehicleId: Int) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = setOf(vehicleId),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun rightVehicleApproach(vehicleId: Int) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(vehicleId),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 3.0,
)

private fun leftAndRightVehicleApproach() = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = setOf(1),
    sideBySideRightVehicleIds = setOf(2),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = 3.0,
)

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
    sectorFlags: List<SectorFlagState> = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = sectorFlags,
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private fun damage(overheating: Boolean) = LmuWindowsVehicleDamageData(
    overheating = overheating,
    partDetached = false,
    lastImpactMagnitude = 0.0,
)

private fun tyreTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsTyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to fl,
        WheelIndex.FRONT_RIGHT to fr,
        WheelIndex.REAR_LEFT to rl,
        WheelIndex.REAR_RIGHT to rr,
    ),
)

private fun tyreWear(
    fl: Double = 1.0,
    fr: Double = 1.0,
    rl: Double = 1.0,
    rr: Double = 1.0,
) = LmuWindowsTyreWearData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to fl,
        WheelIndex.FRONT_RIGHT to fr,
        WheelIndex.REAR_LEFT to rl,
        WheelIndex.REAR_RIGHT to rr,
    ),
)

private fun remainingVirtualEnergy(remainingRatio: Double = 1.0, session: Int = 0) = LmuWindowsVirtualEnergyData(
    remainingRatio = remainingRatio,
    session = session,
)
