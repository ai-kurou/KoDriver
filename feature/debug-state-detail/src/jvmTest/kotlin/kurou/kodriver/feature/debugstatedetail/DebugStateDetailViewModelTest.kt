package kurou.kodriver.feature.debugstatedetail

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var flagRepository: LmuWindowsFlagRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var virtualEnergyRepository: LmuWindowsVirtualEnergyRepository

    @MockK
    private lateinit var lmuWindowsRepository: LmuWindowsRepository

    @MockK
    private lateinit var gt7Ps5Repository: Gt7Ps5Repository

    @MockK
    private lateinit var vehicleApproachRepository: LmuWindowsVehicleApproachRepository

    @MockK(relaxUnitFun = true)
    private lateinit var cardOrderRepository: DebugStateCardOrderPreferencesRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DebugStateDetailViewModel(
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
        observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
        observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(virtualEnergyRepository),
        observeLmuWindowsTelemetry = ObserveLmuWindowsUseCase(lmuWindowsRepository),
        observeGt7Ps5Telemetry = ObserveGt7Ps5UseCase(gt7Ps5Repository),
        observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(vehicleApproachRepository),
        observeCardOrder = ObserveDebugStateCardOrderUseCase(cardOrderRepository),
        resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
        saveCardOrder = SaveDebugStateCardOrderUseCase(cardOrderRepository),
    )

    private fun sampleVirtualEnergy(session: Int) = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = session)

    private fun sampleLmuWindowsTelemetry(currentLap: Int) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing = LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = 0L,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
        vehicle = LmuWindowsVehicleData(
            localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
            positionX = 0.0, positionY = 0.0, positionZ = 0.0,
        ),
    )

    private fun sampleGt7Ps5Telemetry(lapCount: Int) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = 0f,
        gasCapacity = 0f,
    )

    private fun sampleVehicleApproach(leftVehicleIds: Set<Int>) = LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = leftVehicleIds,
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = if (leftVehicleIds.isEmpty()) Double.MAX_VALUE else 1.0,
        lateralDistanceRightMeters = Double.MAX_VALUE,
    )

    @Test
    fun `フラグ情報を未取得の場合は uiState の raceFlags が null`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.UNKNOWN, state.raceFlags?.gamePhase)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `フラグ情報を購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        val flagsFlow = MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GARAGE))
        every { flagRepository.flagStream() } returns flagsFlow
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        flagsFlow.update { sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG) }
        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.GREEN_FLAG, state.raceFlags?.gamePhase)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `選択中シミュレータを購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(Simulator.LmuWindows, state.selectedSimulator)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `バーチャルエナジー情報を購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(10))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(10, state.virtualEnergy?.session)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `LMUテレメトリを購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(3))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(3, state.lmuWindowsTelemetry?.timing?.currentLap)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `GT7テレメトリを購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(5))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(5, state.gt7Ps5Telemetry?.lapCount)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `並走車両情報を購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(setOf(1)))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(true, state.vehicleApproach?.isSideBySideLeft)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `初期状態のcardOrderはデフォルト順序`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(
            listOf(
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
                DebugStateCardKey.YELLOW_FLAG_STATE,
                DebugStateCardKey.CURRENT_LAP,
                DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                DebugStateCardKey.BEST_LAP,
                DebugStateCardKey.TYRE_TEMPERATURE,
                DebugStateCardKey.TYRE_WEAR,
                DebugStateCardKey.FUEL_CONSUMPTION,
            ),
            state.cardOrder,
        )
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `永続化された順序があればそれを初期値として使う`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns
            MutableStateFlow(listOf(DebugStateCardKey.FUEL_CONSUMPTION, DebugStateCardKey.SIMULATOR))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(
            listOf(
                DebugStateCardKey.FUEL_CONSUMPTION,
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
                DebugStateCardKey.YELLOW_FLAG_STATE,
                DebugStateCardKey.CURRENT_LAP,
                DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                DebugStateCardKey.BEST_LAP,
                DebugStateCardKey.TYRE_TEMPERATURE,
                DebugStateCardKey.TYRE_WEAR,
            ),
            state.cardOrder,
        )
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    @Test
    fun `moveCardで順序を入れ替えるとuiStateへ即座に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
        every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
        every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
        every { vehicleApproachRepository.vehicleApproachStream() } returns
            MutableStateFlow(sampleVehicleApproach(emptySet()))
        every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()

        viewModel.moveCard(0, 1)
        val state = viewModel.uiState.first()

        assertEquals(
            listOf(
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
                DebugStateCardKey.YELLOW_FLAG_STATE,
                DebugStateCardKey.CURRENT_LAP,
                DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                DebugStateCardKey.BEST_LAP,
                DebugStateCardKey.TYRE_TEMPERATURE,
                DebugStateCardKey.TYRE_WEAR,
                DebugStateCardKey.FUEL_CONSUMPTION,
            ),
            state.cardOrder,
        )
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
        verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
        verify(exactly = 1) { gt7Ps5Repository.telemetryStream() }
        verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
        verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
        coVerify(exactly = 1) {
            cardOrderRepository.saveCardOrder(
                listOf(
                    DebugStateCardKey.FLAG_INFO,
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.GAME_PHASE,
                    DebugStateCardKey.SESSION,
                    DebugStateCardKey.YELLOW_FLAG_STATE,
                    DebugStateCardKey.CURRENT_LAP,
                    DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    DebugStateCardKey.BEST_LAP,
                    DebugStateCardKey.TYRE_TEMPERATURE,
                    DebugStateCardKey.TYRE_WEAR,
                    DebugStateCardKey.FUEL_CONSUMPTION,
                ),
            )
        }
        confirmVerified(
            simulatorPreferencesRepository,
            flagRepository,
            virtualEnergyRepository,
            lmuWindowsRepository,
            gt7Ps5Repository,
            vehicleApproachRepository,
            cardOrderRepository,
        )
    }

    private fun sampleRaceFlags(gamePhase: SessionPhase) = LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
        startLight = 0,
        numRedLights = 0,
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )
}
