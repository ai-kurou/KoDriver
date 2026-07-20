package kurou.kodriver.feature.main

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
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
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import org.junit.After
import org.junit.Before
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
    private lateinit var exitConfirmationEnabledRepository: ExitConfirmationEnabledRepository

    @MockK
    private lateinit var dynamicColorEnabledRepository: DynamicColorEnabledRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        tagName: String? = null,
        version: String = "1.0.0",
        exitConfirmationEnabled: Boolean = true,
        dynamicColorEnabled: Boolean = false,
    ): Pair<AppScreenViewModel, MutableStateFlow<Boolean>> {
        val exitConfirmationEnabledFlow = MutableStateFlow(exitConfirmationEnabled)
        coEvery { appUpdateRepository.getLatestRelease() } returns tagName?.let { AppUpdate(it) }
        every { keepScreenOnRepository.keepScreenOn() } returns flowOf(false)
        every { exitConfirmationEnabledRepository.exitConfirmationEnabled() } returns exitConfirmationEnabledFlow
        coEvery { exitConfirmationEnabledRepository.saveExitConfirmationEnabled(false) } answers {
            exitConfirmationEnabledFlow.value = false
        }
        every { dynamicColorEnabledRepository.dynamicColorEnabled() } returns flowOf(dynamicColorEnabled)

        return AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            currentVersion = version,
            observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(exitConfirmationEnabledRepository),
            saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(exitConfirmationEnabledRepository),
            observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorEnabledRepository),
        ) to exitConfirmationEnabledFlow
    }

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() = runTest {
        val (viewModel) = createViewModel(tagName = "v9.9.9")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        confirmVerified(appUpdateRepository)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        val (viewModel) = createViewModel(tagName = "v1.0.0")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        confirmVerified(appUpdateRepository)
    }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalse`() = runTest {
        val (viewModel) = createViewModel(tagName = "v9.9.9")

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 0) { appUpdateRepository.getLatestRelease() }
        confirmVerified(appUpdateRepository)
    }

    @Test
    fun `currentVersionが空文字の場合hasAppUpdateがfalseのまま`() = runTest {
        val (viewModel) = createViewModel(tagName = "v9.9.9", version = "")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 0) { appUpdateRepository.getLatestRelease() }
        confirmVerified(appUpdateRepository)
    }

    @Test
    fun `リリース情報が取得できない場合hasAppUpdateがfalseになる`() = runTest {
        val (viewModel) = createViewModel(tagName = null)

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        confirmVerified(appUpdateRepository)
    }

    @Test
    fun `終了確認が有効な場合exitConfirmationEnabledがtrueになる`() = runTest {
        val (viewModel) = createViewModel(exitConfirmationEnabled = true)

        assertTrue(viewModel.uiState.first().exitConfirmationEnabled)
    }

    @Test
    fun `終了確認が無効な場合exitConfirmationEnabledがfalseになる`() = runTest {
        val (viewModel) = createViewModel(exitConfirmationEnabled = false)

        assertFalse(viewModel.uiState.first().exitConfirmationEnabled)
    }

    @Test
    fun `saveExitConfirmationEnabledを呼ぶとリポジトリに保存される`() = runTest {
        val (viewModel, exitConfirmationEnabledFlow) = createViewModel(exitConfirmationEnabled = true)

        viewModel.saveExitConfirmationEnabled(false)

        assertFalse(exitConfirmationEnabledFlow.value)
        verify(exactly = 1) { exitConfirmationEnabledRepository.exitConfirmationEnabled() }
        coVerify(exactly = 1) {
            exitConfirmationEnabledRepository.saveExitConfirmationEnabled(false)
        }
        confirmVerified(exitConfirmationEnabledRepository)
    }

    @Test
    fun `saveExitConfirmationEnabledをfalseで呼ぶとuiStateのexitConfirmationEnabledがfalseになる`() = runTest {
        val (viewModel) = createViewModel(exitConfirmationEnabled = true)

        viewModel.saveExitConfirmationEnabled(false)

        assertFalse(viewModel.uiState.first().exitConfirmationEnabled)
        verify(exactly = 1) { exitConfirmationEnabledRepository.exitConfirmationEnabled() }
        coVerify(exactly = 1) {
            exitConfirmationEnabledRepository.saveExitConfirmationEnabled(false)
        }
        confirmVerified(exitConfirmationEnabledRepository)
    }

    @Test
    fun `Dynamic Colorが有効な場合dynamicColorEnabledがtrueになる`() = runTest {
        val (viewModel) = createViewModel(dynamicColorEnabled = true)

        assertTrue(viewModel.uiState.first().dynamicColorEnabled)
    }

    @Test
    fun `Dynamic Colorが無効な場合dynamicColorEnabledがfalseになる`() = runTest {
        val (viewModel) = createViewModel(dynamicColorEnabled = false)

        assertFalse(viewModel.uiState.first().dynamicColorEnabled)
    }
}
