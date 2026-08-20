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
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
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
    private lateinit var aceWindowsFuelRepository: AceWindowsFuelRepository

    @MockK
    private lateinit var aceWindowsFlagRepository: AceWindowsFlagRepository

    @MockK
    private lateinit var vehicleApproachRepository: LmuWindowsVehicleApproachRepository

    @MockK
    private lateinit var tyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository

    @MockK
    private lateinit var vehicleClassRepository: LmuWindowsVehicleClassRepository

    @MockK
    private lateinit var aceWindowsStatusRepository: AceWindowsStatusRepository

    @MockK
    private lateinit var aceWindowsTyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository

    @MockK
    private lateinit var lmuWindowsPitStatusRepository: LmuWindowsPitStatusRepository

    // saveCardOrder は戻り値 Unit の suspend 関数のため relaxUnitFun でスタブ不要にし、
    // coEvery を省略して coVerify のみで呼び出しを検証する
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

    private fun createViewModel() =
        DebugStateDetailViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
            lmuWindowsUseCases =
                LmuWindowsDebugStateUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(virtualEnergyRepository),
                    observeTelemetry = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(vehicleApproachRepository),
                    observeTyreCarcassTemperature =
                        ObserveLmuWindowsTyreCarcassTemperatureUseCase(tyreCarcassTemperatureRepository),
                    observeVehicleClass = ObserveLmuWindowsVehicleClassUseCase(vehicleClassRepository),
                    observePitStatus = ObserveLmuWindowsPitStatusUseCase(lmuWindowsPitStatusRepository),
                ),
            gt7Ps5UseCases =
                Gt7Ps5DebugStateUseCases(
                    observeTelemetry = ObserveGt7Ps5UseCase(gt7Ps5Repository),
                    observeVehicleClass = ObserveGt7Ps5VehicleClassUseCase(gt7Ps5Repository),
                ),
            aceWindowsUseCases =
                AceWindowsDebugStateUseCases(
                    observeFuel = ObserveAceWindowsFuelUseCase(aceWindowsFuelRepository),
                    observeFlag = ObserveAceWindowsFlagUseCase(aceWindowsFlagRepository),
                    observeStatus = ObserveAceWindowsStatusUseCase(aceWindowsStatusRepository),
                    observeTyreCarcassTemperature =
                        ObserveAceWindowsTyreCarcassTemperatureUseCase(aceWindowsTyreCarcassTemperatureRepository),
                ),
            cardOrderUseCases =
                DebugStateCardOrderUseCases(
                    observeCardOrder = ObserveDebugStateCardOrderUseCase(cardOrderRepository),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                    saveCardOrder = SaveDebugStateCardOrderUseCase(cardOrderRepository),
                ),
        )

    @Test
    fun `フラグ情報を未取得の場合は uiState の raceFlags が null`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(SessionPhase.UNKNOWN, state.raceFlags?.gamePhase)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `フラグ情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            val flagsFlow = MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GARAGE))
            every { flagRepository.flagStream() } returns flagsFlow
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            flagsFlow.update { sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG) }
            val state = viewModel.uiState.first()

            assertEquals(SessionPhase.GREEN_FLAG, state.raceFlags?.gamePhase)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `選択中シミュレータを購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(Simulator.LmuWindows, state.selectedSimulator)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `バーチャルエナジー情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(10))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(10, state.virtualEnergy?.session)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `LMUテレメトリを購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(3))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(3, state.lmuWindowsTelemetry?.timing?.currentLap)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `GT7テレメトリを購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(5))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(5, state.gt7Ps5Telemetry?.lapCount)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `ACEフラッグ情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns
                MutableStateFlow(AceWindowsFlagData(flag = AceWindowsFlagType.BLUE_FLAG))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(AceWindowsFlagType.BLUE_FLAG, state.aceWindowsFlag?.flag)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `並走車両情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(true, state.vehicleApproach?.isSideBySideLeft)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `タイヤカーカス温度情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(
                    LmuWindowsTyreCarcassTemperatureData(
                        wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(92.5f)),
                    ),
                )
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(CelsiusReading(92.5f), state.tyreCarcassTemperature?.wheels?.get(WheelIndex.FRONT_LEFT))
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `車両クラス情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.fromRawValue("LMP2"))
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals("LMP2", state.lmuWindowsVehicleClass?.name)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `GT7車両クラス情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns
                MutableStateFlow(sampleGt7Ps5Telemetry(0, carCategory = "Gr.3"))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals("Gr.3", state.gt7Ps5VehicleClass?.name)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `ACEステータス情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns
                MutableStateFlow(sampleAceWindowsStatus(carLocation = AceWindowsCarLocation.PITLANE))
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(AceWindowsCarLocation.PITLANE, state.aceWindowsStatus?.carLocation)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `LMUピット状態情報を購読すると uiState に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns
                MutableStateFlow(samplePitStatus(pitState = LmuWindowsPitState.ENTERING))
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(LmuWindowsPitState.ENTERING, state.lmuWindowsPitStatus?.pitState)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `初期状態のcardOrderはデフォルト順序`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(
                listOf(
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.VEHICLE_CLASS,
                    DebugStateCardKey.VEHICLE_LOCATION,
                    DebugStateCardKey.FLAG_INFO,
                    DebugStateCardKey.GAME_PHASE,
                    DebugStateCardKey.SESSION,
                    DebugStateCardKey.YELLOW_FLAG_STATE,
                    DebugStateCardKey.CURRENT_LAP,
                    DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    DebugStateCardKey.BEST_LAP,
                    DebugStateCardKey.TYRE_TEMPERATURE,
                    DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    DebugStateCardKey.TYRE_WEAR,
                    DebugStateCardKey.FUEL_CONSUMPTION,
                    DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `永続化された順序があればそれを初期値として使う`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns
                MutableStateFlow(listOf(DebugStateCardKey.FUEL_CONSUMPTION, DebugStateCardKey.SIMULATOR))
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(
                listOf(
                    DebugStateCardKey.FUEL_CONSUMPTION,
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.VEHICLE_CLASS,
                    DebugStateCardKey.VEHICLE_LOCATION,
                    DebugStateCardKey.FLAG_INFO,
                    DebugStateCardKey.GAME_PHASE,
                    DebugStateCardKey.SESSION,
                    DebugStateCardKey.YELLOW_FLAG_STATE,
                    DebugStateCardKey.CURRENT_LAP,
                    DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    DebugStateCardKey.BEST_LAP,
                    DebugStateCardKey.TYRE_TEMPERATURE,
                    DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    DebugStateCardKey.TYRE_WEAR,
                    DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `moveCardで順序を入れ替えるとuiStateへ即座に反映される`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            viewModel.moveCard(0, 1)
            val state = viewModel.uiState.first()

            assertEquals(
                listOf(
                    DebugStateCardKey.VEHICLE_CLASS,
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.VEHICLE_LOCATION,
                    DebugStateCardKey.FLAG_INFO,
                    DebugStateCardKey.GAME_PHASE,
                    DebugStateCardKey.SESSION,
                    DebugStateCardKey.YELLOW_FLAG_STATE,
                    DebugStateCardKey.CURRENT_LAP,
                    DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    DebugStateCardKey.BEST_LAP,
                    DebugStateCardKey.TYRE_TEMPERATURE,
                    DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    DebugStateCardKey.TYRE_WEAR,
                    DebugStateCardKey.FUEL_CONSUMPTION,
                    DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            coVerify(exactly = 1) {
                cardOrderRepository.saveCardOrder(
                    listOf(
                        DebugStateCardKey.VEHICLE_CLASS,
                        DebugStateCardKey.SIMULATOR,
                        DebugStateCardKey.VEHICLE_LOCATION,
                        DebugStateCardKey.FLAG_INFO,
                        DebugStateCardKey.GAME_PHASE,
                        DebugStateCardKey.SESSION,
                        DebugStateCardKey.YELLOW_FLAG_STATE,
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.TYRE_TEMPERATURE,
                        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                        DebugStateCardKey.TYRE_WEAR,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    ),
                )
            }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `LMU選択時は受信済みのLMU対応カードを有効にする`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(10))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(3))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(0))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val enabledCardKeys = viewModel.uiState.first().enabledCardKeys

            assertEquals(defaultDebugStateCardOrder.toSet(), enabledCardKeys)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `GT7選択時は受信済みカードのうちGT7対応カードだけを有効にする`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(3))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val enabledCardKeys = viewModel.uiState.first().enabledCardKeys

            assertEquals(
                setOf(
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.VEHICLE_CLASS,
                    DebugStateCardKey.CURRENT_LAP,
                    DebugStateCardKey.BEST_LAP,
                    DebugStateCardKey.FUEL_CONSUMPTION,
                ),
                enabledCardKeys,
            )
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `ACE選択時は受信済みカードのうちACE対応カードだけを有効にする`() =
        runTest {
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { gt7Ps5Repository.telemetryStream() } returns MutableStateFlow(sampleGt7Ps5Telemetry(3))
            every { aceWindowsFuelRepository.fuelStream() } returns MutableStateFlow(sampleAceWindowsFuel())
            every { aceWindowsFlagRepository.flagStream() } returns MutableStateFlow(sampleAceWindowsFlag())
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { aceWindowsStatusRepository.statusStream() } returns MutableStateFlow(sampleAceWindowsStatus())
            every { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleAceWindowsTyreCarcassTemperature())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()

            val enabledCardKeys = viewModel.uiState.first().enabledCardKeys

            assertEquals(
                setOf(
                    DebugStateCardKey.SIMULATOR,
                    DebugStateCardKey.VEHICLE_LOCATION,
                    DebugStateCardKey.FLAG_INFO,
                    DebugStateCardKey.FUEL_CONSUMPTION,
                    DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                ),
                enabledCardKeys,
            )
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 2) { gt7Ps5Repository.telemetryStream() }
            verify(exactly = 1) { aceWindowsFuelRepository.fuelStream() }
            verify(exactly = 1) { aceWindowsFlagRepository.flagStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { aceWindowsStatusRepository.statusStream() }
            verify(exactly = 1) { aceWindowsTyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                simulatorPreferencesRepository,
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                gt7Ps5Repository,
                aceWindowsFuelRepository,
                aceWindowsFlagRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                vehicleClassRepository,
                aceWindowsStatusRepository,
                aceWindowsTyreCarcassTemperatureRepository,
                lmuWindowsPitStatusRepository,
                cardOrderRepository,
            )
        }
}

private fun sampleRaceFlags(gamePhase: SessionPhase) =
    LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
        startLight = 0,
        numRedLights = 0,
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )

private fun sampleVirtualEnergy(session: Int) = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = session)

private fun sampleLmuWindowsTelemetry(currentLap: Int) =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
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

private fun sampleVehicleApproach(leftVehicleIds: Set<Int>) =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = leftVehicleIds,
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = if (leftVehicleIds.isEmpty()) Double.MAX_VALUE else 1.0,
        lateralDistanceRightMeters = Double.MAX_VALUE,
    )

private fun sampleTyreCarcassTemperature() = LmuWindowsTyreCarcassTemperatureData(wheels = emptyMap())

private fun sampleVehicleClass() = LmuWindowsVehicleClassData.fromRawValue("Hypercar")

private fun sampleGt7Ps5Telemetry(
    lapCount: Int,
    carCategory: String = "",
) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 0,
    bestLapTimeMs = 0,
    gasLevel = 0f,
    gasCapacity = 0f,
    carCategory = carCategory,
)

private fun sampleAceWindowsFuel() = AceWindowsFuelData(remainingPercent = FuelPercent(50.0))

private fun sampleAceWindowsFlag() = AceWindowsFlagData(flag = AceWindowsFlagType.NO_FLAG)

private fun sampleAceWindowsStatus(carLocation: AceWindowsCarLocation = AceWindowsCarLocation.TRACK) =
    AceWindowsStatusData(status = AceWindowsStatusType.LIVE, carLocation = carLocation)

private fun sampleAceWindowsTyreCarcassTemperature() = AceWindowsTyreCarcassTemperatureData(wheels = emptyMap())

private fun samplePitStatus(pitState: LmuWindowsPitState = LmuWindowsPitState.NONE) =
    LmuWindowsPitStatusData(inPits = false, pitState = pitState, inGarageStall = false)
