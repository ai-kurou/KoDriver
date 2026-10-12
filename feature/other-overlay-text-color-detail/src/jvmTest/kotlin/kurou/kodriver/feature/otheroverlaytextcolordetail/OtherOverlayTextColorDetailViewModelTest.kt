package kurou.kodriver.feature.otheroverlaytextcolordetail

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
            saveOverlayTextColor = SaveOverlayTextColorUseCase(repository),
        )

    @Test
    fun `保存値と更新を反映し選択とキャンセルでは保存しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            val viewModel = createViewModel()
            assertEquals(OtherOverlayTextColorDetailUiState(), viewModel.uiState.first())
            flow.update { 0xFFFFFFFFL }
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().selectedOverlayTextColor)
            viewModel.onDismiss()
            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 0) { repository.saveOverlayTextColor(0xFFFF0000L) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
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
            confirmVerified(repository)
        }

    @Test
    fun `保存失敗では保存済みの色を維持する`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } throws IllegalStateException("failed")
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().selectedOverlayTextColor)
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            confirmVerified(repository)
        }

    @Test
    fun `保存のキャンセルでは保存値を変更しない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } throws CancellationException("cancelled")
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, flow.first())
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            confirmVerified(repository)
        }

    @Test
    fun `保存中の選び直しは先行保存で失われない`() =
        runTest(dispatcher) {
            every { repository.observeOverlayTextColor() } returns flow
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } coAnswers { saved.await() }
            val viewModel = createViewModel()
            viewModel.onPendingOverlayTextColorSelected(0xFFFFFFFFL)
            viewModel.onConfirm()
            viewModel.onPendingOverlayTextColorSelected(0xFFFF0000L)
            saved.complete(Unit)
            assertEquals(0xFFFF0000L, viewModel.uiState.first().pendingOverlayTextColor)
            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            verify(exactly = 1) { repository.observeOverlayTextColor() }
            confirmVerified(repository)
        }
}
