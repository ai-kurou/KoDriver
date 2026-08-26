package kurou.kodriver.feature.main

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
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
    ): AppScreenViewModel {
        coEvery { appUpdateRepository.getLatestRelease() } returns tagName?.let { AppUpdate(it) }
        every { keepScreenOnRepository.keepScreenOn() } returns flowOf(false)
        every { dynamicColorEnabledRepository.dynamicColorEnabled() } returns flowOf(dynamicColorEnabled)
        every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns flowOf(hapticFeedbackEnabled)

        return AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            currentVersion = version,
            observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorEnabledRepository),
            observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
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
}
