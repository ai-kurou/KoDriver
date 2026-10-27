package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.lifecycle.viewModelScope
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitState
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.lmuwindows.model.PrimaryFlag
import kurou.kodriver.domain.lmuwindows.model.SectorFlagState
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.SessionYellowFlagState
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions", "LongMethod")
class LmuWindowsDebugStateDetailViewModelTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val hotkeyRepository = FakeReadoutHotkeyRepository()

    private val requestDebugReadout: RequestDebugReadoutUseCase = mockk(relaxUnitFun = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val flagRepository: LmuWindowsFlagRepository = mockk()

    private val virtualEnergyRepository: LmuWindowsVirtualEnergyRepository = mockk()

    private val lmuWindowsRepository: LmuWindowsRepository = mockk()

    private val vehicleApproachRepository: LmuWindowsVehicleApproachRepository = mockk()

    private val tyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository = mockk()

    private val brakeWearRepository: LmuWindowsBrakeWearRepository = mockk()

    private val brakeTemperatureRepository: LmuWindowsBrakeTemperatureRepository = mockk()

    private val vehicleClassRepository: LmuWindowsVehicleClassRepository = mockk()

    private val lmuWindowsPitStatusRepository: LmuWindowsPitStatusRepository = mockk()

    private val vehicleDamageRepository: LmuWindowsVehicleDamageRepository = mockk()

    private val carGapRepository: LmuWindowsCarGapRepository = mockk()
    private val tyreDetachedRepository: LmuWindowsTyreDetachedRepository = mockk()

    // saveCardOrder は戻り値 Unit の suspend 関数のため relaxUnitFun でスタブ不要にし、
    // coEvery を省略して coVerify のみで呼び出しを検証する
    private val cardOrderRepository: DebugStateCardOrderPreferencesRepository = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        currentTimeMs: () -> Long = { 0L },
        sideBySideTickIntervalMs: Long = 100L,
    ) = LmuWindowsDebugStateDetailViewModel(
        requestDebugReadout = requestDebugReadout,
        hotkeyUseCases = hotkeyRepository.useCases(),
        hotkeyCheckTimeoutMs = 1_000L,
        currentTimeMs = currentTimeMs,
        sideBySideTickIntervalMs = sideBySideTickIntervalMs,
        lmuWindowsUseCases =
            LmuWindowsDebugStateUseCases(
                observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
                observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(virtualEnergyRepository),
                observeTelemetry = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(vehicleApproachRepository),
                observeTyreCarcassTemperature =
                    ObserveLmuWindowsTyreCarcassTemperatureUseCase(tyreCarcassTemperatureRepository),
                observeBrakeWear =
                    ObserveLmuWindowsBrakeWearRemainingUseCase(brakeWearRepository, vehicleClassRepository),
                observeBrakeTemperature = ObserveLmuWindowsBrakeTemperatureUseCase(brakeTemperatureRepository),
                observeVehicleClass = ObserveLmuWindowsVehicleClassUseCase(vehicleClassRepository),
                observePitStatus = ObserveLmuWindowsPitStatusUseCase(lmuWindowsPitStatusRepository),
                observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(vehicleDamageRepository),
                observeCarGap = ObserveLmuWindowsCarGapUseCase(carGapRepository),
                observeTyreDetached = ObserveLmuWindowsTyreDetachedUseCase(tyreDetachedRepository),
            ),
        cardOrderUseCases =
            DebugStateCardOrderUseCases(
                observeCardOrder = ObserveDebugStateCardOrderUseCase(cardOrderRepository),
                resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                saveCardOrder = SaveDebugStateCardOrderUseCase(cardOrderRepository),
            ),
    )

    @Test
    fun `ホットキーの設定と状態を表示し動作確認の検出とタイムアウトと再待機を扱う`() =
        runTest {
            every { flagRepository.flagStream() } returns emptyFlow()
            every { virtualEnergyRepository.virtualEnergyStream() } returns emptyFlow()
            every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns emptyFlow()
            every { brakeWearRepository.brakeWearStream() } returns emptyFlow()
            every { vehicleClassRepository.vehicleClassStream() } returns emptyFlow()
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns emptyFlow()
            every { vehicleDamageRepository.vehicleDamageStream() } returns emptyFlow()
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
            try {
                assertEquals(
                    0x70,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.virtualKeyCode,
                )
                hotkeyRepository.registration.update { HotkeyRegistrationState.Registered }
                assertEquals(
                    HotkeyRegistrationState.Registered,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.registrationState,
                )
                viewModel.selectHotkey(0x41)
                assertEquals(1, hotkeyRepository.retryCount)
                assertEquals(
                    0x41,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.virtualKeyCode,
                )
                hotkeyRepository.presses.emit(Unit)
                assertEquals(
                    HotkeyCheckState.Idle,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                viewModel.checkHotkey()
                assertEquals(
                    HotkeyCheckState.Waiting,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                testScheduler.advanceTimeBy(999)
                hotkeyRepository.presses.emit(Unit)
                assertEquals(
                    HotkeyCheckState.Detected,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                viewModel.checkHotkey()
                testScheduler.advanceTimeBy(1_000)
                testScheduler.runCurrent()
                assertEquals(
                    HotkeyCheckState.TimedOut,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                viewModel.checkHotkey()
                testScheduler.advanceTimeBy(500)
                viewModel.checkHotkey()
                testScheduler.advanceTimeBy(500)
                assertEquals(
                    HotkeyCheckState.Waiting,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                viewModel.selectHotkey(0x7B)
                testScheduler.advanceTimeBy(1_000)
                assertEquals(
                    HotkeyCheckState.Idle,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.checkState,
                )
                assertEquals(
                    0x7B,
                    viewModel.uiState
                        .first()
                        .readoutHotkey.virtualKeyCode,
                )
            } finally {
                collection.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `未受信でもシミュレーターカードは常に有効になる`() =
        runTest {
            every { flagRepository.flagStream() } returns emptyFlow()
            every { virtualEnergyRepository.virtualEnergyStream() } returns emptyFlow()
            every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
            every { vehicleApproachRepository.vehicleApproachStream() } returns emptyFlow()
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns emptyFlow()
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns emptyFlow()
            every { brakeWearRepository.brakeWearStream() } returns emptyFlow()
            every { vehicleClassRepository.vehicleClassStream() } returns emptyFlow()
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns emptyFlow()
            every { vehicleDamageRepository.vehicleDamageStream() } returns emptyFlow()
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns emptyFlow()
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            val keys = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows)
            assertEquals(keys.first(), viewModel.uiState.first().selectedReadoutItemKey)
            viewModel.requestReadout()
            viewModel.updateDebugReadoutText(" \n\t ")
            viewModel.requestReadout()
            viewModel.selectReadoutItem("unknown")
            viewModel.selectReadoutItem(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled.value)
            viewModel.selectReadoutItem("foreign_simulator_key")
            assertEquals(keys.first(), viewModel.uiState.first().selectedReadoutItemKey)
            viewModel.selectReadoutItem(keys.last().value)
            viewModel.updateDebugReadoutText("入力 {laps}")
            viewModel.requestReadout()
            val inputState = viewModel.uiState.first { it.debugReadoutText == "入力 {laps}" }
            assertEquals(keys.last(), inputState.selectedReadoutItemKey)
            coVerify(exactly = 1) {
                requestDebugReadout(DebugReadoutRequest(Simulator.LmuWindows, keys.last(), "入力 {laps}"))
            }

            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(setOf(LmuWindowsDebugStateCardKey.SIMULATOR), state.enabledCardKeys)
            assertEquals(LmuWindowsDebugState(), state.lmuWindows)
            assertEquals(defaultDebugStateCardOrder, state.cardOrder)
            confirmVerified(requestDebugReadout)
        }

    @Test
    fun `ブレーキ残量の取得と更新と取得失敗をuiStateに反映する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            val brakeWearFlow = MutableStateFlow<LmuWindowsBrakeWearData?>(sampleBrakeWear())
            every { brakeWearRepository.brakeWearStream() } returns brakeWearFlow
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val initial = viewModel.uiState.first()
            val initialWheel =
                initial.lmuWindows.brakeWear
                    ?.wheels
                    ?.get(WheelIndex.FRONT_LEFT)
            assertEquals(100f, initialWheel?.remainingPercent)
            assertTrue(LmuWindowsDebugStateCardKey.BRAKE_WEAR in initial.enabledCardKeys)

            brakeWearFlow.update {
                LmuWindowsBrakeWearData(mapOf(WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.03f)))
            }
            testScheduler.runCurrent()
            val updated = viewModel.uiState.first()
            val updatedWheel =
                updated.lmuWindows.brakeWear
                    ?.wheels
                    ?.get(WheelIndex.FRONT_LEFT)
            assertEquals(BrakeThicknessMeters(0.03f), updatedWheel?.thickness)
            assertTrue((updatedWheel?.remainingPercent ?: 100f) < 100f)

            brakeWearFlow.update { null }
            testScheduler.runCurrent()
            assertEquals(
                null,
                viewModel.uiState
                    .first()
                    .lmuWindows.brakeWear,
            )
        }

    @Test
    fun `車両時間差の購読結果と更新をuiStateに反映する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns emptyFlow()
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            val gapFlow = MutableStateFlow(LmuWindowsCarGapData(1.0, -2.0, 0.0, 4.0))
            every { carGapRepository.carGapStream() } returns gapFlow
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            assertEquals(
                gapFlow.value,
                viewModel.uiState
                    .first()
                    .lmuWindows.carGap,
            )
            assertTrue(LmuWindowsDebugStateCardKey.CAR_GAP in viewModel.uiState.first().enabledCardKeys)

            gapFlow.update { LmuWindowsCarGapData(-3.5, 0.0, 5.0, -6.0) }
            testScheduler.runCurrent()

            assertEquals(
                gapFlow.value,
                viewModel.uiState
                    .first()
                    .lmuWindows.carGap,
            )
            verify(exactly = 1) { carGapRepository.carGapStream() }
            confirmVerified(carGapRepository)
        }

    @Test
    fun `ブレーキ残量が未受信でも他の状態を表示する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns emptyFlow()
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()
            assertEquals(null, state.lmuWindows.brakeWear)
            assertFalse(LmuWindowsDebugStateCardKey.CAR_GAP in state.enabledCardKeys)
            assertEquals(null, state.lmuWindows.carGap)
            assertFalse(LmuWindowsDebugStateCardKey.BRAKE_WEAR in state.enabledCardKeys)
            assertTrue(LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE in state.enabledCardKeys)
        }

    @Test
    fun `不明なゲームフェーズも uiState に反映する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(SessionPhase.UNKNOWN, state.lmuWindows.raceFlags?.gamePhase)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `フラッグ情報を購読すると uiState に反映される`() =
        runTest {
            val flagsFlow = MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GARAGE))
            every { flagRepository.flagStream() } returns flagsFlow
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            flagsFlow.update { sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG) }
            val state = viewModel.uiState.first()

            assertEquals(SessionPhase.GREEN_FLAG, state.lmuWindows.raceFlags?.gamePhase)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `バーチャルエネルギー情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(10))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(10, state.lmuWindows.virtualEnergy?.session)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `LMUテレメトリを購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(3))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(
                3,
                state.lmuWindows.telemetry
                    ?.timing
                    ?.currentLap,
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `並走車両情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(true, state.lmuWindows.vehicleApproach?.isSideBySideLeft)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `並走時間を注入した時計で計測して uiState に反映する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            val approach = MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { vehicleApproachRepository.vehicleApproachStream() } returns approach
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            var nowMs = 1_000L
            val viewModel = createViewModel(currentTimeMs = { nowMs })
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(LmuWindowsSideBySideDurations(0, null), state.lmuWindows.sideBySideDurations)
            nowMs = 2_250L
            approach.update { it.copy(lateralDistanceLeftMeters = LateralDistanceMeters(1.5)) }
            val continued = viewModel.uiState.first { it.lmuWindows.sideBySideDurations?.leftMillis == 1_250L }
            assertEquals(LmuWindowsSideBySideDurations(1_250, null), continued.lmuWindows.sideBySideDurations)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `並走中は接近データが変わらなくても周期的に経過時間を更新する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            val approach = MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { vehicleApproachRepository.vehicleApproachStream() } returns approach
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            var nowMs = 1_000L
            val viewModel = createViewModel(currentTimeMs = { nowMs }, sideBySideTickIntervalMs = 100L)
            testScheduler.runCurrent()

            val initial = viewModel.uiState.first()
            assertEquals(LmuWindowsSideBySideDurations(0, null), initial.lmuWindows.sideBySideDurations)
            nowMs = 3_000L
            testScheduler.advanceTimeBy(100L)
            val ticked = viewModel.uiState.first { it.lmuWindows.sideBySideDurations?.leftMillis == 2_000L }
            assertEquals(LmuWindowsSideBySideDurations(2_000, null), ticked.lmuWindows.sideBySideDurations)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `右側の並走を停止すると周期更新を止め再開時はゼロから計測する`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            val rightApproach =
                sampleVehicleApproach(emptySet()).copy(
                    sideBySideRightVehicleIds = setOf(2),
                    lateralDistanceRightMeters = LateralDistanceMeters(1.5),
                )
            val approach = MutableStateFlow(rightApproach)
            every { vehicleApproachRepository.vehicleApproachStream() } returns approach
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            var nowMs = 1_000L
            var clockReadCount = 0
            val viewModel =
                createViewModel(
                    currentTimeMs = {
                        clockReadCount++
                        nowMs
                    },
                    sideBySideTickIntervalMs = 100L,
                )
            testScheduler.runCurrent()

            val initial = viewModel.uiState.first()
            assertEquals(LmuWindowsSideBySideDurations(null, 0), initial.lmuWindows.sideBySideDurations)
            nowMs = 3_000L
            testScheduler.advanceTimeBy(100L)
            val ticked = viewModel.uiState.first { it.lmuWindows.sideBySideDurations?.rightMillis == 2_000L }
            assertEquals(LmuWindowsSideBySideDurations(null, 2_000), ticked.lmuWindows.sideBySideDurations)

            approach.update { sampleVehicleApproach(emptySet()) }
            val stopped =
                viewModel.uiState.first {
                    it.lmuWindows.sideBySideDurations == LmuWindowsSideBySideDurations(null, null)
                }
            assertEquals(LmuWindowsSideBySideDurations(null, null), stopped.lmuWindows.sideBySideDurations)
            val stoppedClockReadCount = clockReadCount
            nowMs = 4_000L
            testScheduler.advanceTimeBy(200L)
            testScheduler.runCurrent()
            assertEquals(
                LmuWindowsSideBySideDurations(null, null),
                viewModel.uiState
                    .first()
                    .lmuWindows.sideBySideDurations,
            )
            assertEquals(stoppedClockReadCount, clockReadCount)

            approach.update { rightApproach }
            val restarted = viewModel.uiState.first { it.lmuWindows.sideBySideDurations?.rightMillis == 0L }
            assertEquals(LmuWindowsSideBySideDurations(null, 0), restarted.lmuWindows.sideBySideDurations)
            nowMs = 4_500L
            testScheduler.advanceTimeBy(100L)
            val continued = viewModel.uiState.first { it.lmuWindows.sideBySideDurations?.rightMillis == 500L }
            assertEquals(LmuWindowsSideBySideDurations(null, 500), continued.lmuWindows.sideBySideDurations)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `タイヤカーカス温度情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(
                    LmuWindowsTyreCarcassTemperatureData(
                        wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(92.5f)),
                    ),
                )
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(
                CelsiusReading(92.5f),
                state.lmuWindows.tyreCarcassTemperature
                    ?.wheels
                    ?.get(WheelIndex.FRONT_LEFT),
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `車両クラス情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.fromRawValue("LMP2"))
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals("LMP2", state.lmuWindows.vehicleClass?.name)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `LMUピット状態情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns
                MutableStateFlow(samplePitStatus(pitState = LmuWindowsPitState.ENTERING))
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(LmuWindowsPitState.ENTERING, state.lmuWindows.pitStatus?.pitState)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `車両故障情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns
                MutableStateFlow(sampleVehicleDamage(overheating = true, partDetached = true))
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(true, state.lmuWindows.vehicleDamage?.overheating)
            assertEquals(true, state.lmuWindows.vehicleDamage?.partDetached)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `タイヤ脱落情報を購読すると uiState に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns
                MutableStateFlow(sampleTyreDetached(WheelIndex.FRONT_LEFT))
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(
                true,
                state.lmuWindows.tyreDetached
                    ?.wheels
                    ?.get(WheelIndex.FRONT_LEFT),
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `初期状態のcardOrderはデフォルト順序`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(
                listOf(
                    LmuWindowsDebugStateCardKey.SIMULATOR,
                    LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
                    LmuWindowsDebugStateCardKey.VEHICLE_LOCATION,
                    LmuWindowsDebugStateCardKey.FLAG_INFO,
                    LmuWindowsDebugStateCardKey.GAME_PHASE,
                    LmuWindowsDebugStateCardKey.SESSION,
                    LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE,
                    LmuWindowsDebugStateCardKey.CURRENT_LAP,
                    LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    LmuWindowsDebugStateCardKey.CAR_GAP,
                    LmuWindowsDebugStateCardKey.BEST_LAP,
                    LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_WEAR,
                    LmuWindowsDebugStateCardKey.TYRE_WEAR,
                    LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
                    LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION,
                    LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `永続化された順序があればそれを初期値として使う`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns
                MutableStateFlow(listOf("FUEL_CONSUMPTION", "SIMULATOR"))
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val state = viewModel.uiState.first()

            assertEquals(
                listOf(
                    LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION,
                    LmuWindowsDebugStateCardKey.SIMULATOR,
                    LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
                    LmuWindowsDebugStateCardKey.VEHICLE_LOCATION,
                    LmuWindowsDebugStateCardKey.FLAG_INFO,
                    LmuWindowsDebugStateCardKey.GAME_PHASE,
                    LmuWindowsDebugStateCardKey.SESSION,
                    LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE,
                    LmuWindowsDebugStateCardKey.CURRENT_LAP,
                    LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    LmuWindowsDebugStateCardKey.CAR_GAP,
                    LmuWindowsDebugStateCardKey.BEST_LAP,
                    LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_WEAR,
                    LmuWindowsDebugStateCardKey.TYRE_WEAR,
                    LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
                    LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `moveCardで順序を入れ替えるとuiStateへ即座に反映される`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
            every { virtualEnergyRepository.virtualEnergyStream() } returns MutableStateFlow(sampleVirtualEnergy(0))
            every { lmuWindowsRepository.telemetryStream() } returns MutableStateFlow(sampleLmuWindowsTelemetry(0))
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(emptySet()))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns emptyFlow()
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            viewModel.moveCard(0, 1)
            val state = viewModel.uiState.first()

            val expectedCardOrder =
                listOf(
                    LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
                    LmuWindowsDebugStateCardKey.SIMULATOR,
                    LmuWindowsDebugStateCardKey.VEHICLE_LOCATION,
                    LmuWindowsDebugStateCardKey.FLAG_INFO,
                    LmuWindowsDebugStateCardKey.GAME_PHASE,
                    LmuWindowsDebugStateCardKey.SESSION,
                    LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE,
                    LmuWindowsDebugStateCardKey.CURRENT_LAP,
                    LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                    LmuWindowsDebugStateCardKey.CAR_GAP,
                    LmuWindowsDebugStateCardKey.BEST_LAP,
                    LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE,
                    LmuWindowsDebugStateCardKey.BRAKE_WEAR,
                    LmuWindowsDebugStateCardKey.TYRE_WEAR,
                    LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
                    LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION,
                    LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
                )
            assertEquals(expectedCardOrder, state.cardOrder)
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            coVerify(exactly = 1) { cardOrderRepository.saveCardOrder(expectedCardOrder.map { it.name }) }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }

    @Test
    fun `受信済みのLMU対応カードを有効にする`() =
        runTest {
            every { flagRepository.flagStream() } returns
                MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG))
            val energyFlow = MutableStateFlow(sampleVirtualEnergy(10))
            every { virtualEnergyRepository.virtualEnergyStream() } returns energyFlow
            val telemetryFlow = MutableStateFlow(baselineTelemetry(3, 0.5))
            every { lmuWindowsRepository.telemetryStream() } returns telemetryFlow
            every { vehicleApproachRepository.vehicleApproachStream() } returns
                MutableStateFlow(sampleVehicleApproach(setOf(1)))
            every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
                MutableStateFlow(sampleTyreCarcassTemperature())
            every { brakeTemperatureRepository.brakeTemperatureStream() } returns
                MutableStateFlow(sampleBrakeTemperature())
            every { brakeWearRepository.brakeWearStream() } returns MutableStateFlow(sampleBrakeWear())
            every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(sampleVehicleClass())
            every { lmuWindowsPitStatusRepository.pitStatusStream() } returns MutableStateFlow(samplePitStatus())
            every { vehicleDamageRepository.vehicleDamageStream() } returns MutableStateFlow(sampleVehicleDamage())
            every { carGapRepository.carGapStream() } returns
                MutableStateFlow(LmuWindowsCarGapData(1.0, -2.0, 0.0, 4.0))
            every { tyreDetachedRepository.tyreDetachedStream() } returns MutableStateFlow(sampleTyreDetached())
            every { cardOrderRepository.observeCardOrder() } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel()
            testScheduler.runCurrent()

            val enabledCardKeys = viewModel.uiState.first().enabledCardKeys

            assertEquals(defaultDebugStateCardOrder.toSet(), enabledCardKeys)
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(0.5, 3),
                viewModel.uiState
                    .first()
                    .lmuWindows.virtualEnergyBaseline,
            )
            assertEquals(
                LmuWindowsTyreWearBaseline(0.5, 3),
                viewModel.uiState
                    .first()
                    .lmuWindows.tyreWearBaseline,
            )
            telemetryFlow.update { baselineTelemetry(5, 0.4) }
            energyFlow.update { it.copy(remainingRatio = LmuWindowsVirtualEnergyRatio(0.4)) }
            testScheduler.runCurrent()
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(0.5, 3),
                viewModel.uiState
                    .first()
                    .lmuWindows.virtualEnergyBaseline,
            )
            assertEquals(
                LmuWindowsTyreWearBaseline(0.5, 3),
                viewModel.uiState
                    .first()
                    .lmuWindows.tyreWearBaseline,
            )
            energyFlow.update { it.copy(remainingRatio = LmuWindowsVirtualEnergyRatio(0.8)) }
            testScheduler.runCurrent()
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(0.8, 5),
                viewModel.uiState
                    .first()
                    .lmuWindows.virtualEnergyBaseline,
            )
            telemetryFlow.update { baselineTelemetry(5, 0.8) }
            testScheduler.runCurrent()
            assertEquals(
                LmuWindowsTyreWearBaseline(0.8, 5),
                viewModel.uiState
                    .first()
                    .lmuWindows.tyreWearBaseline,
            )
            verify(exactly = 1) { flagRepository.flagStream() }
            verify(exactly = 1) { virtualEnergyRepository.virtualEnergyStream() }
            verify(exactly = 1) { lmuWindowsRepository.telemetryStream() }
            verify(exactly = 1) { vehicleApproachRepository.vehicleApproachStream() }
            verify(exactly = 1) { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() }
            verify(exactly = 1) { brakeTemperatureRepository.brakeTemperatureStream() }
            verify(exactly = 1) { brakeWearRepository.brakeWearStream() }
            verify(exactly = 2) { vehicleClassRepository.vehicleClassStream() }
            verify(exactly = 1) { lmuWindowsPitStatusRepository.pitStatusStream() }
            verify(exactly = 1) { vehicleDamageRepository.vehicleDamageStream() }
            verify(exactly = 1) { carGapRepository.carGapStream() }
            verify(exactly = 1) { tyreDetachedRepository.tyreDetachedStream() }
            verify(exactly = 1) { cardOrderRepository.observeCardOrder() }
            confirmVerified(
                flagRepository,
                virtualEnergyRepository,
                lmuWindowsRepository,
                vehicleApproachRepository,
                tyreCarcassTemperatureRepository,
                brakeTemperatureRepository,
                brakeWearRepository,
                vehicleClassRepository,
                lmuWindowsPitStatusRepository,
                vehicleDamageRepository,
                carGapRepository,
                tyreDetachedRepository,
                cardOrderRepository,
            )
        }
}

private fun sampleRaceFlags(gamePhase: SessionPhase) =
    LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
    )

private fun sampleVirtualEnergy(session: Int) =
    LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5), session = session)

private fun sampleLmuWindowsTelemetry(currentLap: Int) =
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
        lateralDistanceLeftMeters =
            LateralDistanceMeters(if (leftVehicleIds.isEmpty()) Double.MAX_VALUE else 1.0),
        lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
    )

private fun sampleTyreCarcassTemperature() = LmuWindowsTyreCarcassTemperatureData(wheels = emptyMap())

private fun sampleBrakeTemperature() = LmuWindowsBrakeTemperatureData(wheels = emptyMap())

private fun sampleVehicleClass() = LmuWindowsVehicleClassData.fromRawValue("Hypercar")

private fun samplePitStatus(pitState: LmuWindowsPitState = LmuWindowsPitState.NONE) =
    LmuWindowsPitStatusData(inPits = false, pitState = pitState, inGarageStall = false)

private fun sampleVehicleDamage(
    overheating: Boolean = false,
    partDetached: Boolean = false,
) = LmuWindowsVehicleDamageData(overheating = overheating, partDetached = partDetached, lastImpactMagnitude = 0.0)

private fun sampleTyreDetached(vararg detachedWheels: WheelIndex) =
    LmuWindowsTyreDetachedData(wheels = WheelIndex.entries.associateWith { it in detachedWheels })

private fun sampleBrakeWear() = LmuWindowsBrakeWearData(mapOf(WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.036f)))

private fun baselineTelemetry(
    lap: Int,
    ratio: Double,
): LmuWindowsTelemetryData =
    sampleLmuWindowsTelemetry(lap).copy(
        tyres =
            LmuWindowsTyreData(
                wheels =
                    mapOf(
                        WheelIndex.FRONT_LEFT to
                            LmuWindowsTyreWheelData(
                                surfaceTemperature = CelsiusReading(0f),
                                carcassTemperature = CelsiusReading(0f),
                                brakeTemperature = CelsiusReading(0f),
                                pressureKpa = PressureKpa(0.0),
                                wear = LmuWindowsTyreWearRatio(ratio),
                            ),
                    ),
            ),
    )
