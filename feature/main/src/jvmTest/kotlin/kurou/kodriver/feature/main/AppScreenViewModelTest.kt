package kurou.kodriver.feature.main

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveEffectiveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryReceivingUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppScreenViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var appUpdateRepository: AppUpdateRepository

    @MockK
    private lateinit var keepScreenOnRepository: KeepScreenOnEnabledRepository

    @MockK
    private lateinit var dynamicColorEnabledRepository: DynamicColorEnabledRepository

    @MockK
    private lateinit var hapticFeedbackEnabledRepository: HapticFeedbackEnabledRepository

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var lmuWindowsRepository: LmuWindowsRepository

    @MockK
    private lateinit var gt7Ps5Repository: Gt7Ps5Repository

    @MockK
    private lateinit var aceWindowsStatusRepository: AceWindowsStatusRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        tagName: String? = null,
        version: String = "1.0.0",
        keepScreenOnEnabled: Boolean = false,
        isTelemetryReceiving: Boolean = false,
        dynamicColorEnabled: Boolean = false,
        hapticFeedbackEnabled: Boolean = true,
        selectedSimulator: Simulator = Simulator.LmuWindows,
    ): AppScreenViewModel {
        coEvery { appUpdateRepository.getLatestRelease() } returns tagName?.let { AppUpdate(it) }
        every { keepScreenOnRepository.keepScreenOn() } returns flowOf(keepScreenOnEnabled)
        every { dynamicColorEnabledRepository.dynamicColorEnabled() } returns flowOf(dynamicColorEnabled)
        every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns flowOf(hapticFeedbackEnabled)
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(selectedSimulator)
        stubTelemetryStreams(selectedSimulator, isTelemetryReceiving)

        return AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            currentVersion = version,
            observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorEnabledRepository),
            observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            observeEffectiveKeepScreenOn =
                ObserveEffectiveKeepScreenOnUseCase(
                    observeKeepScreenOnEnabled = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
                    observeTelemetryReceiving =
                        ObserveTelemetryReceivingUseCase(
                            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
                            observeLmuWindows = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                            observeGt7Ps5 = ObserveGt7Ps5UseCase(gt7Ps5Repository),
                            observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(aceWindowsStatusRepository),
                        ),
                ),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
        )
    }

    private fun stubTelemetryStreams(
        selectedSimulator: Simulator,
        isTelemetryReceiving: Boolean,
    ) {
        when (selectedSimulator) {
            is Simulator.LmuWindows -> {
                every { lmuWindowsRepository.telemetryStream() } returns
                    if (isTelemetryReceiving) flowOf(FAKE_LMU_WINDOWS_TELEMETRY_DATA) else emptyFlow()
            }

            is Simulator.Gt7Ps5 -> {
                every { gt7Ps5Repository.telemetryStream() } returns
                    if (isTelemetryReceiving) flowOf(FAKE_GT7_PS5_TELEMETRY_DATA) else emptyFlow()
            }

            is Simulator.AceWindows -> {
                every { aceWindowsStatusRepository.statusStream() } returns
                    if (isTelemetryReceiving) flowOf(FAKE_ACE_WINDOWS_STATUS_DATA) else emptyFlow()
            }
        }
    }

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() =
        runTest {
            val viewModel = createViewModel(tagName = "v9.9.9")

            viewModel.checkUpdate()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
            confirmVerified(appUpdateRepository)
        }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() =
        runTest {
            val viewModel = createViewModel(tagName = "v1.0.0")

            viewModel.checkUpdate()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
            confirmVerified(appUpdateRepository)
        }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalse`() =
        runTest {
            val viewModel = createViewModel(tagName = "v9.9.9")

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 0) { appUpdateRepository.getLatestRelease() }
            confirmVerified(appUpdateRepository)
        }

    @Test
    fun `currentVersionが空文字の場合hasAppUpdateがfalseのまま`() =
        runTest {
            val viewModel = createViewModel(tagName = "v9.9.9", version = "")

            viewModel.checkUpdate()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 0) { appUpdateRepository.getLatestRelease() }
            confirmVerified(appUpdateRepository)
        }

    @Test
    fun `リリース情報が取得できない場合hasAppUpdateがfalseになる`() =
        runTest {
            val viewModel = createViewModel(tagName = null)

            viewModel.checkUpdate()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.first().hasAppUpdate)
            coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
            confirmVerified(appUpdateRepository)
        }

    @Test
    fun `Dynamic Colorが有効な場合dynamicColorEnabledがtrueになる`() =
        runTest {
            val viewModel = createViewModel(dynamicColorEnabled = true)

            assertTrue(viewModel.uiState.first().dynamicColorEnabled)
        }

    @Test
    fun `Dynamic Colorが無効な場合dynamicColorEnabledがfalseになる`() =
        runTest {
            val viewModel = createViewModel(dynamicColorEnabled = false)

            assertFalse(viewModel.uiState.first().dynamicColorEnabled)
        }

    @Test
    fun `ハプティックフィードバックが有効な場合hapticFeedbackEnabledがtrueになる`() =
        runTest {
            val viewModel = createViewModel(hapticFeedbackEnabled = true)

            assertTrue(viewModel.uiState.first().hapticFeedbackEnabled)
        }

    @Test
    fun `ハプティックフィードバックが無効な場合hapticFeedbackEnabledがfalseになる`() =
        runTest {
            val viewModel = createViewModel(hapticFeedbackEnabled = false)

            assertFalse(viewModel.uiState.first().hapticFeedbackEnabled)
        }

    @Test
    fun `選択済みシミュレータがuiStateに反映される`() =
        runTest {
            val viewModel = createViewModel(selectedSimulator = Simulator.LmuWindows)
            val uiState = viewModel.uiState.first()

            assertEquals(Simulator.LmuWindows, uiState.selectedSimulator)
            assertEquals("lmu_windows", uiState.selectedSimulatorId)
        }

    @Test
    fun `selectSimulatorを呼ぶとsaveSelectedSimulatorが実行される`() =
        runTest {
            val viewModel = createViewModel()
            coEvery { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) } returns Unit

            viewModel.selectSimulator("gt7_ps5")
            advanceUntilIdle()

            coVerify(exactly = 1) { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) }
        }

    @Test
    fun `selectSimulatorに未対応のIDを渡した場合saveSelectedSimulatorは実行されない`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectSimulator("unknown_simulator")
            advanceUntilIdle()

            coVerify(exactly = 0) { simulatorRepository.saveSelectedSimulator(any()) }
        }

    @Test
    fun `スリープさせない設定が有効かつテレメトリ受信中の場合keepScreenOnがtrueになる`() =
        runTest {
            val viewModel =
                createViewModel(
                    keepScreenOnEnabled = true,
                    isTelemetryReceiving = true,
                    selectedSimulator = Simulator.Gt7Ps5,
                )

            assertTrue(viewModel.uiState.first().keepScreenOn)
        }

    @Test
    fun `スリープさせない設定が有効でもテレメトリ未受信の場合keepScreenOnはfalseになる`() =
        runTest {
            val viewModel =
                createViewModel(
                    keepScreenOnEnabled = true,
                    isTelemetryReceiving = false,
                    selectedSimulator = Simulator.Gt7Ps5,
                )

            assertFalse(viewModel.uiState.first().keepScreenOn)
        }

    @Test
    fun `テレメトリ受信中でもスリープさせない設定が無効な場合keepScreenOnはfalseになる`() =
        runTest {
            val viewModel =
                createViewModel(
                    keepScreenOnEnabled = false,
                    isTelemetryReceiving = true,
                    selectedSimulator = Simulator.Gt7Ps5,
                )

            assertFalse(viewModel.uiState.first().keepScreenOn)
        }

    private companion object {
        val FAKE_LMU_WINDOWS_TELEMETRY_DATA =
            LmuWindowsTelemetryData(
                timestampMs = 0L,
                engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
                inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
                tyres = LmuWindowsTyreData(wheels = emptyMap()),
                fuel =
                    LmuWindowsFuelData(
                        currentLiters = LmuWindowsFuelUnit(0.0),
                        capacityLiters = LmuWindowsFuelUnit(0.0),
                    ),
                timing =
                    LmuWindowsTimingData(
                        currentLapTimeMs = 0L,
                        lastLapTimeMs = 0L,
                        bestLapTimeMs = 0L,
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
        val FAKE_GT7_PS5_TELEMETRY_DATA =
            Gt7Ps5TelemetryData(
                lapCount = 0,
                lapsInRace = 0,
                bestLapTimeMs = -1,
                gasLevel = Gt7Ps5FuelUnit(0f),
                gasCapacity = Gt7Ps5FuelUnit(100f),
            )
        val FAKE_ACE_WINDOWS_STATUS_DATA = AceWindowsStatusData(status = AceWindowsStatusType.LIVE)
    }
}
