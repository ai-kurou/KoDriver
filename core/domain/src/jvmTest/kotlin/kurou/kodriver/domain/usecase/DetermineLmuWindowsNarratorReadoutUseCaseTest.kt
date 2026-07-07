package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleData
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

        assertEquals(listOf(SpeechEvent.MyBestLapCasual), second.events)
        assertEquals(59_000L, second.state.personalBestMs)
    }

    @Test
    fun `自己ベストラップ項目が無効なら読み上げない`() {
        val first = useCase.determineMyBestLap(
            state = LmuWindowsNarratorState(),
            telemetry = telemetry(bestLapTimeMs = 60_000L),
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.MyBestLap to false)),
        )

        val second = useCase.determineMyBestLap(
            state = first.state,
            telemetry = telemetry(bestLapTimeMs = 59_000L),
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.MyBestLap to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左接近が50ms継続するとCarLeftを返す`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftProximity(vehicleId = 1),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftProximity(vehicleId = 1),
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
            proximity = rightProximity(vehicleId = 1),
            settings = settings(startReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = rightProximity(vehicleId = 1),
            settings = settings(startReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH),
            observedAtMs = 50L,
        )

        assertEquals(listOf(SpeechEvent.RightApproach), second.events)
    }

    @Test
    fun `50ms未満の接近では読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftProximity(vehicleId = 1),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftProximity(vehicleId = 1),
            settings = settings(),
            observedAtMs = 49L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `左右同時接近は読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftAndRightProximity(),
            settings = settings(),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftAndRightProximity(),
            settings = settings(),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `接近読み上げ無効時は状態だけ更新する`() {
        val decision = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftProximity(vehicleId = 1),
            settings = settings(startReadoutEnabled = false),
            observedAtMs = 0L,
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertNotNull(decision.state.vehicleApproachState.left[1])
    }

    @Test
    fun `1周目スキップ中の0周目は接近を読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftProximity(vehicleId = 1),
            settings = settings(skipFirstLap = true, currentLap = 0),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftProximity(vehicleId = 1),
            settings = settings(skipFirstLap = true, currentLap = 0),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
    }

    @Test
    fun `車両接近項目が無効なら接近を読み上げない`() {
        val first = useCase.determineVehicleApproach(
            state = LmuWindowsNarratorState(),
            proximity = leftProximity(vehicleId = 1),
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.VehicleApproach to false)),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftProximity(vehicleId = 1),
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.VehicleApproach to false)),
            observedAtMs = 50L,
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
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
                    ReadoutItemKey.BlueFlag to false,
                    ReadoutItemKey.SectorYellowFlag to false,
                    ReadoutItemKey.RedFlag to false,
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
                    ReadoutItemKey.Flag to false,
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
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.Overheat to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `車両故障項目が無効なら読み上げない`() {
        val decision = useCase.determineVehicleDamage(
            state = LmuWindowsNarratorState(previousVehicleDamage = damage(overheating = false)),
            vehicleDamage = damage(overheating = true),
            settings = settings(enabledStates = allEnabledStates + mapOf(ReadoutItemKey.VehicleDamage to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }

    @Test
    fun `いずれかのタイヤが閾値以上になると TyreOverheat を返す`() {
        val decision = useCase.determineTyreTemperature(
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
        val decision = useCase.determineTyreTemperature(
            state = state,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() {
        val overheatState = useCase.determineTyreTemperature(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val cooledState = useCase.determineTyreTemperature(
            state = overheatState,
            data = tyreTemperature(fl = 85.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        ).state

        val reovertState = useCase.determineTyreTemperature(
            state = cooledState,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(false, cooledState.tyreOverheating)
        assertEquals(listOf(SpeechEvent.TyreOverheat), reovertState.events)
    }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() {
        val decision = useCase.determineTyreTemperature(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.TyreTemperature to false),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
        assertEquals(true, decision.state.tyreOverheating)
    }

    @Test
    fun `無効中に過熱した状態で再有効化しても読み上げない`() {
        val disabledState = useCase.determineTyreTemperature(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 95.0),
            settings = settings(
                tyreTemperatureHighThresholdCelsius = 90,
                enabledStates = allEnabledStates + mapOf(ReadoutItemKey.TyreTemperature to false),
            ),
        ).state

        val reenabledDecision = useCase.determineTyreTemperature(
            state = disabledState,
            data = tyreTemperature(fl = 95.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(emptyList<SpeechEvent>(), reenabledDecision.events)
    }

    @Test
    fun `閾値ちょうどは高温扱い`() {
        val decision = useCase.determineTyreTemperature(
            state = LmuWindowsNarratorState(),
            data = tyreTemperature(fl = 90.0),
            settings = settings(tyreTemperatureHighThresholdCelsius = 90),
        )

        assertEquals(listOf(SpeechEvent.TyreOverheat), decision.events)
    }
}

private val allEnabledStates: Map<ReadoutItemKey, Boolean> = mapOf(
    ReadoutItemKey.MyBestLap to true,
    ReadoutItemKey.VehicleApproach to true,
    ReadoutItemKey.VehicleDamage to true,
    ReadoutItemKey.Overheat to true,
    ReadoutItemKey.TyreTemperature to true,
    ReadoutItemKey.Flag to true,
    ReadoutItemKey.BlueFlag to true,
    ReadoutItemKey.SectorYellowFlag to true,
    ReadoutItemKey.FullCourseYellow to true,
    ReadoutItemKey.RedFlag to true,
)

private fun settings(
    enabledStates: Map<ReadoutItemKey, Boolean> = allEnabledStates,
    myBestLapVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
    currentLap: Int = 1,
    skipFirstLap: Boolean = false,
    startReadoutEnabled: Boolean = true,
    startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
    tyreTemperatureHighThresholdCelsius: Int = 90,
) = LmuWindowsNarratorReadoutSettings(
    enabledStates = enabledStates,
    myBestLapVoiceType = myBestLapVoiceType,
    currentLap = currentLap,
    skipFirstLap = skipFirstLap,
    vehicleApproachStartReadoutEnabled = startReadoutEnabled,
    vehicleApproachStartReadoutType = startReadoutType,
    tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThresholdCelsius,
)

private fun telemetry(bestLapTimeMs: Long) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = EngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = InputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = TyreData(wheels = emptyMap()),
    fuel = FuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = TimingData(
        currentLapTimeMs = 0L,
        lastLapTimeMs = 0L,
        bestLapTimeMs = bestLapTimeMs,
        sector1Ms = 0L,
        sector2Ms = 0L,
        currentLap = 0,
        maxLaps = 0,
    ),
    vehicle = VehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    ),
)

private fun leftProximity(vehicleId: Int) = LmuWindowsProximityData(
    sideBySideLeftVehicleIds = setOf(vehicleId),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun rightProximity(vehicleId: Int) = LmuWindowsProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(vehicleId),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 3.0,
)

private fun leftAndRightProximity() = LmuWindowsProximityData(
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
