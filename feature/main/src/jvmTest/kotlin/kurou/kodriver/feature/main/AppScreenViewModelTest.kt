package kurou.kodriver.feature.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppScreenViewModelTest {

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
        val viewModel = AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository("v9.9.9")),
            currentVersion = "1.0.0",
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository("v1.0.0")),
            currentVersion = "1.0.0",
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `初期状態ではhasAppUpdateがfalse`() = runTest {
        val viewModel = AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(null)),
            currentVersion = "1.0.0",
        )

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }

    @Test
    fun `リリース情報が取得できない場合hasAppUpdateがfalseになる`() = runTest {
        val viewModel = AppScreenViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(null)),
            currentVersion = "1.0.0",
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
    }
}

private class FakeAppUpdateRepository(private val tagName: String?) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = tagName?.let { AppUpdate(it) }
}
