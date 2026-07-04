package kurou.kodriver.feature.otherthemedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherThemeDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeThemePreferencesRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeThemePreferencesRepository(initialThemeMode = ThemeMode.SYSTEM)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        repo: FakeThemePreferencesRepository = repository,
    ) = OtherThemeDetailViewModel(
        observeThemeMode = ObserveThemeModeUseCase(repo),
        saveThemeMode = SaveThemeModeUseCase(repo),
    )

    @Test
    fun `保存済みテーマモードをUI状態に反映する`() = runTest(dispatcher) {
        repository.saveThemeMode(ThemeMode.DARK)
        val viewModel = createViewModel()

        assertEquals(
            OtherThemeDetailUiState(
                selectedThemeMode = ThemeMode.DARK,
                pendingThemeMode = ThemeMode.DARK,
            ),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `テーマモードを選択するとpendingThemeModeだけが変わる`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onPendingThemeModeSelected(ThemeMode.LIGHT)

        assertEquals(
            OtherThemeDetailUiState(
                selectedThemeMode = ThemeMode.SYSTEM,
                pendingThemeMode = ThemeMode.LIGHT,
            ),
            viewModel.uiState.first(),
        )
        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }

    @Test
    fun `onConfirmを呼ぶとpendingThemeModeを保存する`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onPendingThemeModeSelected(ThemeMode.DARK)
        viewModel.onConfirm()

        assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
    }

    @Test
    fun `pendingThemeModeがない状態でonConfirmを呼んでも保存済み値は変わらない`() = runTest(dispatcher) {
        repository.saveThemeMode(ThemeMode.LIGHT)
        val viewModel = createViewModel()

        viewModel.onConfirm()

        assertEquals(ThemeMode.LIGHT, repository.observeThemeMode().first())
    }

    @Test
    fun `onDismissを呼ぶとpendingThemeModeを保存済み値に戻す`() = runTest(dispatcher) {
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
        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }
}
