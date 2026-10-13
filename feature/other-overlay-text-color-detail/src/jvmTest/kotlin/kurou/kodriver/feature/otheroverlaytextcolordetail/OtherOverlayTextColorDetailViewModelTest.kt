package kurou.kodriver.feature.otheroverlaytextcolordetail

import androidx.lifecycle.ViewModelStore
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.PreviewOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextColorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherOverlayTextColorDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: OverlayTextColorPreferencesRepository = mockk()
    private val flow = MutableStateFlow(OVERLAY_TEXT_COLOR_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherOverlayTextColorDetailViewModel(
            observeOverlayTextColor = ObserveOverlayTextColorUseCase(repository),
            previewOverlayTextColor = PreviewOverlayTextColorUseCase(repository),
            saveOverlayTextColor = SaveOverlayTextColorUseCase(repository),
        )

    @Test
    fun `保存値と更新を反映し選択とキャンセルでは保存しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } answers { flow.update { 0xFFFF0000L } }
            every { repository.setPreviewOverlayTextColor(null) } answers { flow.update { 0xFFFFFFFFL } }
            val viewModel = createViewModel()
            assertEquals(OtherOverlayTextColorDetailUiState(), viewModel.uiState.first())
            flow.update { 0xFFFFFFFFL }
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().selectedOverlayTextColor)
            viewModel.onDismiss()
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 0) { repository.saveOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewStoppedで未保存の選択とプレビューを解除して保存値へ戻す`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } answers { flow.update { 0xFFFF0000L } }
            every { repository.setPreviewOverlayTextColor(null) } answers { flow.update { 0xFFFFFFFFL } }
            val viewModel = createViewModel()
            assertEquals(OtherOverlayTextColorDetailUiState(), viewModel.uiState.first())
            flow.update { 0xFFFFFFFFL }
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().selectedOverlayTextColor)
            viewModel.onPreviewStopped()
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 0) { repository.saveOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `未選択のOKでは保存しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            val viewModel = createViewModel()
            viewModel.onConfirm()
            assertEquals(OtherOverlayTextColorDetailUiState(), viewModel.uiState.first())
            coVerify(exactly = 0) { repository.saveOverlayTextColor(OVERLAY_TEXT_COLOR_DEFAULT) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            confirmVerified(repository)
        }

    @Test
    fun `OKで選択値を保存しリセット値も保存できる`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } answers { flow.update { 0xFFFFFFFFL } }
            coEvery { repository.saveOverlayTextColor(OVERLAY_TEXT_COLOR_DEFAULT) } answers {
                flow.update { OVERLAY_TEXT_COLOR_DEFAULT }
            }
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(OVERLAY_TEXT_COLOR_DEFAULT) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            assertEquals(0xFFFFFFFFL, flow.first())
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            viewModel.onPendingOverlayTextColorSelected(OVERLAY_TEXT_COLOR_DEFAULT)
            viewModel.onConfirm()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, flow.first())
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            coVerify(exactly = 1) { repository.saveOverlayTextColor(OVERLAY_TEXT_COLOR_DEFAULT) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(OVERLAY_TEXT_COLOR_DEFAULT) }
            verify(exactly = 2) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存失敗ではプレビューを解除して保存済みの色に戻す`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } throws IllegalStateException("failed")
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().selectedOverlayTextColor)
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存のキャンセルでは保存値を変更しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } throws CancellationException("cancelled")
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, flow.first())
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存中の選び直しでは先行保存の完了でプレビューを解除しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } coAnswers { saved.await() }
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            saved.complete(Unit)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 0) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `先行保存中の離脱通知は後続プレビューを解除せず保存完了後の離脱で解除できる`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } coAnswers { saved.await() }
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            viewModel.onPreviewStopped()
            verify(exactly = 0) { repository.setPreviewOverlayTextColor(null) }
            saved.complete(Unit)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            verify(exactly = 0) { repository.setPreviewOverlayTextColor(null) }
            viewModel.onPreviewStopped()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存が完了するまでプレビューを解除しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            viewModel.onPreviewStopped()

            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)

            verify(exactly = 0) { repository.setPreviewOverlayTextColor(null) }
            saved.complete(Unit)
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().pendingOverlayTextColor)

            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            confirmVerified(repository)
        }

    @Test
    fun `ViewModel破棄でプレビューを解除する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("overlayTextColor", viewModel)
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)

            store.clear()

            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            coVerify(exactly = 0) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            confirmVerified(repository)
        }

    @Test
    fun `オーバーレイ文字色を選択するとプレビューしてpendingOverlayTextColorが変わる`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            val viewModel = createViewModel()

            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } answers {
                flow.update { 0xFFFF0000L }
            }

            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)

            assertEquals(
                OtherOverlayTextColorDetailUiState(
                    selectedOverlayTextColor = 0xFFFF0000L,
                    pendingOverlayTextColor = 0xFFFF0000L,
                ),
                viewModel.uiState.first(),
            )
            assertEquals(0xFFFF0000L, flow.first())
            coVerify(exactly = 0) { repository.saveOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            confirmVerified(repository)
        }

    @Test
    fun `保存中に破棄して離脱した後の選択は先行保存の完了で重複解除されない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
            every { repository.setPreviewOverlayTextColor(0xFFFF0000L) } returns Unit
            every { repository.setPreviewOverlayTextColor(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            viewModel.onDismiss()
            viewModel.onPreviewStopped()
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            saved.complete(Unit)

            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            confirmVerified(repository)
        }
}
