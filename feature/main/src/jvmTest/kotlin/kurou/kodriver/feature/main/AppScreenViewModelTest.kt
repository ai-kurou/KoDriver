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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
        dynamicColorEnabled: Boolean = false,
        hapticFeedbackEnabled: Boolean = true,
        selectedSimulator: Simulator = Simulator.LmuWindows,
    ): AppScreenViewModel {
        coEvery { appUpdateRepository.getLatestRelease() } returns tagName?.let { AppUpdate(it) }
        every { keepScreenOnRepository.keepScreenOn() } returns flowOf(false)
        every { dynamicColorEnabledRepository.dynamicColorEnabled() } returns flowOf(dynamicColorEnabled)
        every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns flowOf(hapticFeedbackEnabled)
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(selectedSimulator)

        return AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            currentVersion = version,
            observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorEnabledRepository),
            observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
        )
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
}
