package kurou.kodriver.feature.otherthemedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherThemeDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val repository: ThemePreferencesRepository = mockk()

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherThemeDetailViewModel(
            observeThemeMode = ObserveThemeModeUseCase(repository),
            saveThemeMode = SaveThemeModeUseCase(repository),
        )

    @Test
    fun `保存済みテーマモードをUI状態に反映する`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            themeModeFlow.update { ThemeMode.DARK }
            val viewModel = createViewModel()

            assertEquals(
                OtherThemeDetailUiState(
                    selectedThemeMode = ThemeMode.DARK,
                    pendingThemeMode = ThemeMode.DARK,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }

    @Test
    fun `テーマモードを選択するとpendingThemeModeだけが変わる`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val viewModel = createViewModel()

            viewModel.onPendingThemeModeSelected(ThemeMode.LIGHT)

            assertEquals(
                OtherThemeDetailUiState(
                    selectedThemeMode = ThemeMode.SYSTEM,
                    pendingThemeMode = ThemeMode.LIGHT,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(ThemeMode.SYSTEM, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }

    @Test
    fun `onConfirmを呼ぶとpendingThemeModeを保存する`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } answers { themeModeFlow.update { ThemeMode.DARK } }
            val viewModel = createViewModel()

            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            viewModel.onConfirm()

            assertEquals(ThemeMode.DARK, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }

    @Test
    fun `pendingThemeModeがない状態でonConfirmを呼んでも保存済み値は変わらない`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            themeModeFlow.update { ThemeMode.LIGHT }
            val viewModel = createViewModel()

            viewModel.onConfirm()

            assertEquals(ThemeMode.LIGHT, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }

    @Test
    fun `onDismissを呼ぶとpendingThemeModeを保存済み値に戻す`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val viewModel = createViewModel()

            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            viewModel.onDismiss()

            assertEquals(
                OtherThemeDetailUiState(
                    selectedThemeMode = ThemeMode.SYSTEM,
                    pendingThemeMode = ThemeMode.SYSTEM,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(ThemeMode.SYSTEM, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }

    @Test
    fun `リポジトリのテーマモードが変わるとselectedThemeModeに反映される`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val viewModel = createViewModel()

            themeModeFlow.update { ThemeMode.DARK }

            assertEquals(
                OtherThemeDetailUiState(
                    selectedThemeMode = ThemeMode.DARK,
                    pendingThemeMode = ThemeMode.DARK,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }

    @Test
    fun `確定後も保存済み状態へ反映されるまでプレビューを維持する`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val saveCompleted = CompletableDeferred<Unit>()
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } coAnswers { saveCompleted.await() }
            val viewModel = createViewModel()
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }

            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            viewModel.onConfirm()
            assertEquals(ThemeMode.DARK, viewModel.uiState.first().pendingThemeMode)
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first().selectedThemeMode)

            saveCompleted.complete(Unit)
            advanceUntilIdle()
            assertEquals(ThemeMode.DARK, viewModel.uiState.first().pendingThemeMode)
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first().selectedThemeMode)

            themeModeFlow.update { ThemeMode.DARK }
            assertEquals(ThemeMode.DARK, viewModel.uiState.first().pendingThemeMode)
            // プレビュー解除後は保存済み設定の変更に追従する。
            themeModeFlow.update { ThemeMode.LIGHT }
            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first().pendingThemeMode)
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }

    @Test
    fun `保存待ちの間に選び直したプレビューを以前の確定処理で解除しない`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val saveCompleted = CompletableDeferred<Unit>()
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } coAnswers { saveCompleted.await() }
            val viewModel = createViewModel()
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }

            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            viewModel.onConfirm()
            viewModel.onPendingThemeModeSelected(ThemeMode.LIGHT)
            saveCompleted.complete(Unit)
            themeModeFlow.update { ThemeMode.DARK }

            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first().pendingThemeMode)
            viewModel.onDismiss()
            assertEquals(ThemeMode.DARK, viewModel.uiState.first().pendingThemeMode)
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }

    @Test
    fun `保存に失敗するとプレビューを解除して保存済みテーマへ戻す`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            themeModeFlow.update { ThemeMode.LIGHT }
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } throws IOException("保存失敗")
            val viewModel = createViewModel()
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, viewModel.uiState.first().pendingThemeMode)

            viewModel.onConfirm()

            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first().pendingThemeMode)
            assertEquals(ThemeMode.LIGHT, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }

    @Test
    fun `保存失敗時も保存待ちの間に選び直した別のプレビューは解除しない`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val saveCompleted = CompletableDeferred<Unit>()
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } coAnswers {
                saveCompleted.await()
                throw IOException("保存失敗")
            }
            val viewModel = createViewModel()
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
            viewModel.onConfirm()
            viewModel.onPendingThemeModeSelected(ThemeMode.LIGHT)

            saveCompleted.complete(Unit)

            assertEquals(ThemeMode.LIGHT, viewModel.uiState.first().pendingThemeMode)
            assertEquals(ThemeMode.SYSTEM, themeModeFlow.first())
            viewModel.onDismiss()
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first().pendingThemeMode)
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }

    @Test
    fun `保存のキャンセルを再送出してプレビューを解除する`() =
        runTest(dispatcher) {
            every { repository.observeThemeMode() } returns themeModeFlow
            val saveJob = CompletableDeferred<Job>()
            coEvery { repository.saveThemeMode(ThemeMode.DARK) } coAnswers {
                saveJob.complete(currentCoroutineContext().job)
                throw CancellationException("保存のキャンセル")
            }
            val viewModel = createViewModel()
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            viewModel.onPendingThemeModeSelected(ThemeMode.DARK)

            viewModel.onConfirm()

            assertTrue(saveJob.await().isCancelled)
            assertEquals(ThemeMode.SYSTEM, viewModel.uiState.first().pendingThemeMode)
            assertEquals(ThemeMode.SYSTEM, themeModeFlow.first())
            verify(exactly = 1) { repository.observeThemeMode() }
            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.DARK) }
            confirmVerified(repository)
        }
}
