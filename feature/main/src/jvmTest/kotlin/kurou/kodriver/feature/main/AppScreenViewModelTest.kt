package kurou.kodriver.feature.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppScreenViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
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
    ) = AppScreenViewModel(
        checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(tagName)),
        currentVersion = version,
        observeKeepScreenOn = ObserveKeepScreenOnUseCase(FakeKeepScreenOnRepository()),
        observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(
            FakeExitConfirmationPreferencesRepository(exitConfirmationEnabled),
        ),
    )

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() = runTest {
        val viewModel = createViewModel(tagName = "v9.9.9")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = createViewModel(tagName = "v1.0.0")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalse`() = runTest {
        val viewModel = createViewModel(tagName = "v9.9.9")

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `currentVersionが空文字の場合hasAppUpdateがfalseのまま`() = runTest {
        val viewModel = createViewModel(tagName = "v9.9.9", version = "")

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `リリース情報が取得できない場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = createViewModel(tagName = null)

        viewModel.checkUpdate()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `終了確認が有効な場合exitConfirmationEnabledがtrueになる`() = runTest {
        val viewModel = createViewModel(exitConfirmationEnabled = true)

        assertTrue(viewModel.uiState.first().exitConfirmationEnabled)
    }

    @Test
    fun `終了確認が無効な場合exitConfirmationEnabledがfalseになる`() = runTest {
        val viewModel = createViewModel(exitConfirmationEnabled = false)

        assertFalse(viewModel.uiState.first().exitConfirmationEnabled)
    }
}

private class FakeAppUpdateRepository(private val tagName: String?) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = tagName?.let { AppUpdate(it) }
}

private class FakeKeepScreenOnRepository : KeepScreenOnPreferencesRepository {
    override fun keepScreenOn(): Flow<Boolean> = flowOf(false)
    override suspend fun saveKeepScreenOn(enabled: Boolean) = Unit
}

private class FakeExitConfirmationPreferencesRepository(
    private val enabled: Boolean,
) : ExitConfirmationPreferencesRepository {
    override fun exitConfirmationEnabled(): Flow<Boolean> = flowOf(enabled)
    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) = Unit
}
