package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleDamageData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("TooManyFunctions")
class DetermineLmuWindowsNarratorReadoutUseCaseTest {
    private val useCase = DetermineLmuWindowsNarratorReadoutUseCase()

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
            settings = settings(enabledStates = mapOf(ReadoutItemKey.VehicleApproach to false)),
            observedAtMs = 0L,
        )

        val second = useCase.determineVehicleApproach(
            state = first.state,
            proximity = leftProximity(vehicleId = 1),
            settings = settings(enabledStates = mapOf(ReadoutItemKey.VehicleApproach to false)),
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
                enabledStates = mapOf(
                    ReadoutItemKey.BlueFlag to false,
                    ReadoutItemKey.SectorYellowFlag to false,
                    ReadoutItemKey.RedFlag to false,
                ),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), second.events)
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
            settings = settings(enabledStates = mapOf(ReadoutItemKey.Overheat to false)),
        )

        assertEquals(emptyList<SpeechEvent>(), decision.events)
    }
}

private fun settings(
    enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    currentLap: Int = 1,
    skipFirstLap: Boolean = false,
    startReadoutEnabled: Boolean = true,
    startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
) = LmuWindowsNarratorReadoutSettings(
    enabledStates = enabledStates,
    currentLap = currentLap,
    skipFirstLap = skipFirstLap,
    vehicleApproachStartReadoutEnabled = startReadoutEnabled,
    vehicleApproachStartReadoutType = startReadoutType,
)

private fun leftProximity(vehicleId: Int) = ProximityData(
    sideBySideLeftVehicleIds = setOf(vehicleId),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun rightProximity(vehicleId: Int) = ProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(vehicleId),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 3.0,
)

private fun leftAndRightProximity() = ProximityData(
    sideBySideLeftVehicleIds = setOf(1),
    sideBySideRightVehicleIds = setOf(2),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = 3.0,
)

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
    sectorFlags: List<SectorFlagState> = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
) = RaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = sectorFlags,
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private fun damage(overheating: Boolean) = VehicleDamageData(
    overheating = overheating,
    partDetached = false,
    lastImpactMagnitude = 0.0,
)
