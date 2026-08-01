package kurou.kodriver.feature.otherthemedetail

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherThemeDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: ThemePreferencesRepository

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
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
}
