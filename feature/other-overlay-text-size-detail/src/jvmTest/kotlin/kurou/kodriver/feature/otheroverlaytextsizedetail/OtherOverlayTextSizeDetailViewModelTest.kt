package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.lifecycle.ViewModelStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
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
import kurou.kodriver.domain.usecase.PreviewOverlayTextSizeUseCase
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
            previewOverlayTextSize = PreviewOverlayTextSizeUseCase(repository),
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
    fun `オーバーレイ文字サイズを選択するとプレビューしてpendingOverlayTextSizeが変わる`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            val viewModel = createViewModel()

            every { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) } answers {
                overlayTextSizeFlow.update { OverlayTextSize.SMALL }
            }

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.SMALL)

            assertEquals(
                OtherOverlayTextSizeDetailUiState(
                    selectedOverlayTextSize = OverlayTextSize.SMALL,
                    pendingOverlayTextSize = OverlayTextSize.SMALL,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(OverlayTextSize.SMALL, overlayTextSizeFlow.first())
            coVerify(exactly = 0) { repository.saveOverlayTextSize(OverlayTextSize.SMALL) }
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) }
            confirmVerified(repository)
        }

    @Test
    fun `onConfirmを呼ぶとpendingOverlayTextSizeを保存する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            coEvery {
                repository.saveOverlayTextSize(OverlayTextSize.LARGE)
            } answers { overlayTextSizeFlow.update { OverlayTextSize.LARGE } }
            every { repository.setPreviewOverlayTextSize(null) } returns Unit
            val viewModel = createViewModel()

            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } returns Unit

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onConfirm()

            assertEquals(OverlayTextSize.LARGE, overlayTextSizeFlow.first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
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
            every { repository.setPreviewOverlayTextSize(null) } answers {
                overlayTextSizeFlow.update { OverlayTextSize.MEDIUM }
            }
            val viewModel = createViewModel()

            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } answers {
                overlayTextSizeFlow.update { OverlayTextSize.LARGE }
            }

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
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
            coVerify(exactly = 0) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
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

    @Test
    fun `保存が完了するまでプレビューを解除しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } returns Unit
            every { repository.setPreviewOverlayTextSize(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextSize(OverlayTextSize.LARGE) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onConfirm()

            verify(exactly = 0) { repository.setPreviewOverlayTextSize(null) }
            saved.complete(Unit)

            verify(exactly = 1) { repository.observeOverlayTextSize() }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
            confirmVerified(repository)
        }

    @Test
    fun `ViewModel破棄でプレビューを解除する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } returns Unit
            every { repository.setPreviewOverlayTextSize(null) } returns Unit
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("overlayTextSize", viewModel)
            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)

            store.clear()

            verify(exactly = 1) { repository.observeOverlayTextSize() }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
            coVerify(exactly = 0) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            confirmVerified(repository)
        }

    @Test
    fun `保存に失敗してもプレビューを解除する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } returns Unit
            every { repository.setPreviewOverlayTextSize(null) } returns Unit
            coEvery { repository.saveOverlayTextSize(OverlayTextSize.LARGE) } throws IllegalStateException("failed")
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onConfirm()

            verify(exactly = 1) { repository.observeOverlayTextSize() }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存中に別のサイズを選び直しても先行保存の完了で後続のプレビューを解除しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) } returns Unit
            every { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextSize(OverlayTextSize.LARGE) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.LARGE)
            viewModel.onConfirm()
            viewModel.onPendingOverlayTextSizeSelected(OverlayTextSize.SMALL)
            saved.complete(Unit)

            assertEquals(OverlayTextSize.SMALL, viewModel.uiState.first().pendingOverlayTextSize)
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE) }
            verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) }
            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.LARGE) }
            confirmVerified(repository)
        }
}
