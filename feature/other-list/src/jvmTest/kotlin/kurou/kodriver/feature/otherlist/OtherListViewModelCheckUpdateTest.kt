package kurou.kodriver.feature.otherlist

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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelCheckUpdateTest {

    private val dispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var appUpdateRepository: AppUpdateRepository

    @MockK
    private lateinit var keepScreenOnRepository: KeepScreenOnEnabledRepository

    @MockK
    private lateinit var exitConfirmationRepository: ExitConfirmationEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        every { keepScreenOnRepository.keepScreenOn() } returns MutableStateFlow(true)
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns MutableStateFlow(true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(currentVersion: String) = OtherListViewModel(
        checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
        observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
        saveKeepScreenOn = SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
        observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(exitConfirmationRepository),
        saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(exitConfirmationRepository),
        currentVersion = currentVersion,
        appVersionLabel = "Windows版KoDriverバージョン",
    )

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() = runTest {
        coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v9.9.9")
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v1.0.0")
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalseのまま`() = runTest {
        val viewModel = createViewModel(currentVersion = "1.0.0")
        dispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `currentVersionが空の場合checkUpdateは何もしない`() = runTest {
        val viewModel = createViewModel(currentVersion = "")

        viewModel.checkUpdate()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `リリース情報がnullの場合hasAppUpdateがfalseになる`() = runTest {
        coEvery { appUpdateRepository.getLatestRelease() } returns null
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }
}
