package kurou.kodriver.feature.acewindowsdebugstatedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions", "LongMethod")
class AceWindowsDebugStateDetailViewModelTest {
    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fuelRepository: AceWindowsFuelRepository = mockk()

    private val flagRepository: AceWindowsFlagRepository = mockk()

    private val statusRepository: AceWindowsStatusRepository = mockk()

    private val tyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository = mockk()

    private val vehicleApproachRepository: AceWindowsVehicleApproachRepository = mockk()

    private val bestLapTimeRepository: AceWindowsBestLapTimeRepository = mockk()

    private val remainingFuelLapsRepository: AceWindowsRemainingFuelLapsRepository = mockk()

    // 保存は Unit の suspend 関数のため、呼び出しを coVerify で検証する。
    private val cardOrderRepository: DebugStateCardOrderPreferencesRepository = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsDebugStateDetailViewModel(
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            currentTimeMs = { 1_234L },
            aceWindowsUseCases =
                AceWindowsDebugStateUseCases(
                    observeFuel = ObserveAceWindowsFuelUseCase(fuelRepository),
                    observeFlag = ObserveAceWindowsFlagUseCase(flagRepository),
                    observeStatus = ObserveAceWindowsStatusUseCase(statusRepository),
                    observeTyreCarcassTemperature =
                        ObserveAceWindowsTyreCarcassTemperatureUseCase(
                            tyreCarcassTemperatureRepository,
                        ),
                    observeVehicleApproach = ObserveAceWindowsVehicleApproachUseCase(vehicleApproachRepository),
                    observeBestLapTime = ObserveAceWindowsBestLapTimeUseCase(bestLapTimeRepository),
                    observeRemainingFuelLaps = ObserveAceWindowsRemainingFuelLapsUseCase(remainingFuelLapsRepository),
                ),
            cardOrderUseCases =
                DebugStateCardOrderUseCases(
                    observeCardOrder = ObserveDebugStateCardOrderUseCase(cardOrderRepository),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                    saveCardOrder = SaveDebugStateCardOrderUseCase(cardOrderRepository),
                ),
        )

    @Test
    fun `未受信でもシミュレーターカードは常に有効になる`() =
        runTest {
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(setOf(AceWindowsDebugStateCardKey.SIMULATOR), state.enabledCardKeys)
            assertEquals(AceWindowsDebugState(), state.aceWindows)
            assertEquals(defaultDebugStateCardOrder, state.cardOrder)
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `燃料の受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsFuelData>()
            every { fuelRepository.fuelStream() } returns flow
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsFuelData(remainingPercent = FuelPercent(50.0))
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.fuel)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.FUEL_CONSUMPTION),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `フラッグの受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsFlagData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns flow
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsFlagData(flag = AceWindowsFlagType.GREEN_FLAG)
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.flag)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.FLAG_INFO),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `ステータスの受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsStatusData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns flow
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.TRACK)
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.status)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.VEHICLE_LOCATION),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `タイヤカーカス温度の受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsTyreCarcassTemperatureData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns flow
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsTyreCarcassTemperatureData(wheels = emptyMap())
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.tyreCarcassTemperature)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `周辺車両の受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsVehicleApproachData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns flow
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsVehicleApproachData(nearbyVehicles = emptyList())
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.vehicleApproach)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `ベストラップの受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsBestLapTimeData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns flow
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsBestLapTimeData(bestLapTimeMs = 83_456)
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.bestLapTime)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.BEST_LAP),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `燃料残り周数の受信だけで対応カードを有効にして状態を反映する`() =
        runTest {
            val flow = MutableSharedFlow<AceWindowsRemainingFuelLapsData>()
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns flow
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            val data = AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f)
            flow.emit(data)
            testScheduler.runCurrent()
            val state = viewModel.uiState.first()

            assertEquals(data, state.aceWindows.remainingFuelLaps)
            assertEquals(
                setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.FUEL_CONSUMPTION),
                state.enabledCardKeys,
            )
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `全ストリームを集約し更新しても受信済みカードを維持する`() =
        runTest {
            val flow = MutableStateFlow(AceWindowsFuelData(remainingPercent = FuelPercent(50.0)))
            every { fuelRepository.fuelStream() } returns flow
            every { flagRepository.flagStream() } returns
                MutableStateFlow(AceWindowsFlagData(flag = AceWindowsFlagType.GREEN_FLAG))
            every { statusRepository.statusStream() } returns
                MutableStateFlow(AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.TRACK))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(AceWindowsTyreCarcassTemperatureData(wheels = emptyMap()))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(AceWindowsVehicleApproachData(nearbyVehicles = emptyList()))
            every { bestLapTimeRepository.bestLapTimeStream() } returns
                MutableStateFlow(AceWindowsBestLapTimeData(bestLapTimeMs = 83_456))
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns
                MutableStateFlow(AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f))
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            assertEquals(aceWindowsSupportedCardKeys, viewModel.uiState.first().enabledCardKeys)
            assertEquals(
                AceWindowsDebugState(
                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(50.0)),
                    flag = AceWindowsFlagData(flag = AceWindowsFlagType.GREEN_FLAG),
                    status = AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.TRACK),
                    tyreCarcassTemperature = AceWindowsTyreCarcassTemperatureData(wheels = emptyMap()),
                    vehicleApproach = AceWindowsVehicleApproachData(nearbyVehicles = emptyList()),
                    bestLapTime = AceWindowsBestLapTimeData(bestLapTimeMs = 83_456),
                    remainingFuelLaps = AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f),
                ),
                viewModel.uiState.first().aceWindows,
            )
            flow.update { AceWindowsFuelData(remainingPercent = FuelPercent(42.0)) }
            testScheduler.runCurrent()
            assertEquals(
                FuelPercent(42.0),
                viewModel.uiState
                    .first()
                    .aceWindows.fuel
                    ?.remainingPercent,
            )
            assertEquals(aceWindowsSupportedCardKeys, viewModel.uiState.first().enabledCardKeys)
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `保存済み順序の非対応カードを除外して不足するACEカードを補完する`() =
        runTest {
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            val persisted =
                MutableStateFlow(listOf("BEST_LAP", "VEHICLE_CLASS"))
            every { cardOrderRepository.observeCardOrder() } returns persisted
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()

            assertEquals(
                listOf(AceWindowsDebugStateCardKey.BEST_LAP) +
                    defaultDebugStateCardOrder.filter { it != AceWindowsDebugStateCardKey.BEST_LAP },
                viewModel.uiState.first().cardOrder,
            )
            persisted.update { defaultDebugStateCardOrder.reversed().map { it.name } }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder.reversed(), viewModel.uiState.first().cardOrder)
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `moveCardは即座に順序を反映して保存し保存前の通知でも巻き戻らない`() =
        runTest {
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            val persisted = MutableStateFlow(emptyList<String>())
            every { cardOrderRepository.observeCardOrder() } returns persisted
            val viewModel = createViewModel()
            viewModel.uiState.first()
            testScheduler.runCurrent()
            val expected = defaultDebugStateCardOrder.toMutableList().apply { add(2, removeAt(0)) }

            viewModel.moveCard(0, 2)
            testScheduler.runCurrent()
            assertEquals(expected, viewModel.uiState.first().cardOrder)
            persisted.update { defaultDebugStateCardOrder.reversed().map { it.name } }
            testScheduler.runCurrent()
            assertEquals(expected, viewModel.uiState.first().cardOrder)
            verify(exactly = 1) { fuelRepository.fuelStream() }
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { statusRepository.statusStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { bestLapTimeRepository.bestLapTimeStream() }
            verify(exactly = 1) { remainingFuelLapsRepository.remainingFuelLapsStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            coVerify(exactly = 1) { cardOrderRepository.saveCardOrder(expected.map { it.name }) }
            confirmVerified(
                fuelRepository,
                flagRepository,
                statusRepository,
                tyreCarcassTemperatureRepository,
                vehicleApproachRepository,
                bestLapTimeRepository,
                remainingFuelLapsRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `入力と選択を保存して送信後は文字列だけをクリアする`() =
        runTest {
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val keys = ReadoutItemKey.entriesOf(Simulator.AceWindows)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 1_234L,
                    simulator = Simulator.AceWindows,
                    readoutItemKey = keys.last(),
                    narratedText = "表示テスト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = "{}",
                )
            } returns Unit
            val viewModel = createViewModel()
            assertEquals(keys, viewModel.uiState.first().debugOverlayKeys)
            assertEquals(keys.first(), viewModel.uiState.first().debugOverlaySelectedKey)
            viewModel.onDebugOverlayTextChange("  表示テスト  ")
            viewModel.onDebugOverlayKeySelect(keys.last())
            val input = viewModel.uiState.first { it.debugOverlayText == "  表示テスト  " }
            assertEquals(keys.last(), input.debugOverlaySelectedKey)
            viewModel.sendDebugOverlayText()
            val sent = viewModel.uiState.first { it.debugOverlayText.isEmpty() }
            assertEquals(keys.last(), sent.debugOverlaySelectedKey)
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 1_234L,
                    simulator = Simulator.AceWindows,
                    readoutItemKey = keys.last(),
                    narratedText = "表示テスト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = "{}",
                )
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `空白入力は保存せず他シミュレーターのキーは選択しない`() =
        runTest {
            every { fuelRepository.fuelStream() } returns emptyFlow()
            every { flagRepository.flagStream() } returns emptyFlow()
            every { statusRepository.statusStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { bestLapTimeRepository.bestLapTimeStream() } returns emptyFlow()
            every { remainingFuelLapsRepository.remainingFuelLapsStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val keys = ReadoutItemKey.entriesOf(Simulator.AceWindows)
            val viewModel = createViewModel()
            viewModel.sendDebugOverlayText()
            viewModel.onDebugOverlayTextChange(" \n\t ")
            viewModel.onDebugOverlayKeySelect(ReadoutItemKey.entries.first { it !in keys })
            viewModel.sendDebugOverlayText()
            assertEquals(keys.first(), viewModel.uiState.first().debugOverlaySelectedKey)
            coVerify(exactly = 0) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 1_234L,
                    simulator = Simulator.AceWindows,
                    readoutItemKey = keys.first(),
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = "{}",
                )
            }
            confirmVerified(telemetryLogRepository)
        }
}
