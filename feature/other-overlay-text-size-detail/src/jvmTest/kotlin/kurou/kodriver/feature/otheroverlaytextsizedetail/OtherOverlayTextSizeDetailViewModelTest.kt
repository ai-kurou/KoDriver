package kurou.kodriver.feature.otheroverlaytextsizedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextSizeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherOverlayTextSizeDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val repository: OverlayTextSizePreferencesRepository = mockk()

    private val overlayTextSizeFlow = MutableStateFlow(OverlayTextSize.MEDIUM)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherOverlayTextSizeDetailViewModel(
            observeOverlayTextSize = ObserveOverlayTextSizeUseCase(repository),
            saveOverlayTextSize = SaveOverlayTextSizeUseCase(repository),
        )

    @Test
    fun `保存済みオーバーレイ文字サイズをUI状態に反映する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            overlayTextSizeFlow.update { OverlayTextSize.LARGE }
            val viewModel = createViewModel()

            assertEquals(
                OtherOverlayTextSizeDetailUiState(
                    selectedOverlayTextSize = OverlayTextSize.LARGE,
                    pendingOverlayTextSize = OverlayTextSize.LARGE,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }

    @Test
    fun `オーバーレイ文字サイズを選択するとpendingOverlayTextSizeだけが変わる`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.SMALL)

            assertEquals(
                OtherOverlayTextSizeDetailUiState(
                    selectedOverlayTextSize = OverlayTextSize.MEDIUM,
                    pendingOverlayTextSize = OverlayTextSize.SMALL,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(OverlayTextSize.MEDIUM, overlayTextSizeFlow.first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }

    @Test
    fun `onConfirmを呼ぶとpendingOverlayTextSizeを保存する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            coEvery {
                repository.saveOverlayTextSize(OverlayTextSize.LARGE)
            } answers { overlayTextSizeFlow.update { OverlayTextSize.LARGE } }
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onConfirm()

            assertEquals(OverlayTextSize.LARGE, overlayTextSizeFlow.first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            confirmVerified(repository)
        }

    @Test
    fun `pendingOverlayTextSizeがない状態でonConfirmを呼んでも保存済み値は変わらない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            overlayTextSizeFlow.update { OverlayTextSize.SMALL }
            val viewModel = createViewModel()

            viewModel.onConfirm()

            assertEquals(OverlayTextSize.SMALL, overlayTextSizeFlow.first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }

    @Test
    fun `onDismissを呼ぶとpendingOverlayTextSizeを保存済み値に戻す`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onDismiss()

            assertEquals(
                OtherOverlayTextSizeDetailUiState(
                    selectedOverlayTextSize = OverlayTextSize.MEDIUM,
                    pendingOverlayTextSize = OverlayTextSize.MEDIUM,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(OverlayTextSize.MEDIUM, overlayTextSizeFlow.first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }

    @Test
    fun `リポジトリのオーバーレイ文字サイズが変わるとselectedOverlayTextSizeに反映される`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            val viewModel = createViewModel()

            overlayTextSizeFlow.update { OverlayTextSize.LARGE }

            assertEquals(
                OtherOverlayTextSizeDetailUiState(
                    selectedOverlayTextSize = OverlayTextSize.LARGE,
                    pendingOverlayTextSize = OverlayTextSize.LARGE,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }
}
