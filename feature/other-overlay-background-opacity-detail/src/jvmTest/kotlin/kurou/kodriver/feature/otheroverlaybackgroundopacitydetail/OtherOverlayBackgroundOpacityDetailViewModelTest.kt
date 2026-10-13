package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

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
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.PreviewOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.SaveOverlayBackgroundOpacityUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherOverlayBackgroundOpacityDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: OverlayBackgroundOpacityPreferencesRepository = mockk()

    private val opacityFlow = MutableStateFlow(50)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherOverlayBackgroundOpacityDetailViewModel(
            observeOverlayBackgroundOpacity = ObserveOverlayBackgroundOpacityUseCase(repository),
            previewOverlayBackgroundOpacity = PreviewOverlayBackgroundOpacityUseCase(repository),
            saveOverlayBackgroundOpacity = SaveOverlayBackgroundOpacityUseCase(repository),
        )

    @Test
    fun `保存済みの透明度をUiStateで返す`() =
        runTest {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            val viewModel = createViewModel()

            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(opacity = 50), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            confirmVerified(repository)
        }

    @Test
    fun `透明度を変更するとUiStateが更新される`() =
        runTest {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            coEvery { repository.saveOverlayBackgroundOpacity(80) } answers { opacityFlow.update { 80 } }
            val viewModel = createViewModel()

            viewModel.onOpacityChanged(80)

            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(opacity = 80), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            confirmVerified(repository)
        }

    @Test
    fun `ドラッグ中の透明度をプレビューする`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } answers { opacityFlow.update { 80 } }
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)

            assertEquals(80, viewModel.uiState.first().opacity)
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            coVerify(exactly = 0) { repository.saveOverlayBackgroundOpacity(80) }
            confirmVerified(repository)
        }

    @Test
    fun `保存が完了するまでプレビューを解除しない`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayBackgroundOpacity(80) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)
            viewModel.onOpacityChanged(80)

            verify(exactly = 0) { repository.setPreviewOverlayBackgroundOpacity(null) }
            saved.complete(Unit)

            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            confirmVerified(repository)
        }

    @Test
    fun `ペイン破棄でプレビューを解除し先行保存の完了で解除を重複させない`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayBackgroundOpacity(80) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)
            viewModel.onOpacityChanged(80)
            viewModel.onPreviewStopped()
            saved.complete(Unit)

            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            confirmVerified(repository)
        }

    @Test
    fun `ViewModel破棄でプレビューを解除する`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            val viewModel = createViewModel()
            val store = ViewModelStore()
            store.put("overlayBackgroundOpacity", viewModel)
            viewModel.onOpacityPreview(80)

            store.clear()

            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            coVerify(exactly = 0) { repository.saveOverlayBackgroundOpacity(80) }
            confirmVerified(repository)
        }

    @Test
    fun `保存に失敗してもプレビューを解除する`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            coEvery { repository.saveOverlayBackgroundOpacity(80) } throws IllegalStateException("failed")
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)
            viewModel.onOpacityChanged(80)

            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            confirmVerified(repository)
        }

    @Test
    fun `保存中に別の透明度を選び直しても先行保存の完了で後続のプレビューを解除しない`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(20) } answers { opacityFlow.update { 20 } }
            val saved = CompletableDeferred<Unit>()
            coEvery { repository.saveOverlayBackgroundOpacity(80) } coAnswers { saved.await() }
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)
            viewModel.onOpacityChanged(80)
            viewModel.onOpacityPreview(20)
            saved.complete(Unit)

            assertEquals(20, opacityFlow.first())
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(20) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            confirmVerified(repository)
        }

    @Test
    fun `保存がキャンセルされてもプレビューを解除する`() =
        runTest(testDispatcher) {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
            every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
            coEvery { repository.saveOverlayBackgroundOpacity(80) } throws CancellationException("cancelled")
            val viewModel = createViewModel()

            viewModel.onOpacityPreview(80)
            viewModel.onOpacityChanged(80)

            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
            confirmVerified(repository)
        }
}
