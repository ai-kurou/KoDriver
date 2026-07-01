package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelCheckUpdateTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() = runTest {
        val viewModel = createViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(
                FakeAppUpdateRepository(AppUpdate(tagName = "v9.9.9")),
            ),
            currentVersion = "1.0.0",
        )

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = createViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(
                FakeAppUpdateRepository(AppUpdate(tagName = "v1.0.0")),
            ),
            currentVersion = "1.0.0",
        )

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalseのまま`() = runTest {
        val viewModel = createViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(
                FakeAppUpdateRepository(AppUpdate(tagName = "v9.9.9")),
            ),
            currentVersion = "1.0.0",
        )

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `currentVersionが空の場合checkUpdateは何もしない`() = runTest {
        val viewModel = createViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(
                FakeAppUpdateRepository(AppUpdate(tagName = "v9.9.9")),
            ),
            currentVersion = "",
        )

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `リリース情報がnullの場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = createViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(
                FakeAppUpdateRepository(latestRelease = null),
            ),
            currentVersion = "1.0.0",
        )

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    private fun createViewModel(
        checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
        currentVersion: String,
    ): OtherListViewModel {
        val exitConfirmationPreferencesRepository = FakeExitConfirmationPreferencesRepository()
        val observeExitConfirmationEnabled =
            ObserveExitConfirmationEnabledUseCase(exitConfirmationPreferencesRepository)
        return OtherListViewModel(
            checkAppUpdateAvailable = checkAppUpdateAvailable,
            observeExitConfirmationEnabled = observeExitConfirmationEnabled,
            saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(exitConfirmationPreferencesRepository),
            currentVersion = currentVersion,
            appVersionLabel = "Windows版KoDriverバージョン",
        )
    }
}
