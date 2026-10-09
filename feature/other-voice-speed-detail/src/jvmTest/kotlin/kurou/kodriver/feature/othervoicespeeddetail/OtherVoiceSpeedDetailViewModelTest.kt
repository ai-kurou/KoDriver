package kurou.kodriver.feature.othervoicespeeddetail

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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceSpeedDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: VoiceSpeedPreferencesRepository = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val observeSoundVolume: ObserveSoundVolumeUseCase = mockk()

    private val speedFlow = MutableStateFlow(1.5f)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVoiceSpeedDetailViewModel(
            observeVoiceSpeed = ObserveVoiceSpeedUseCase(repository),
            saveVoiceSpeed = SaveVoiceSpeedUseCase(repository),
            speakText = speakText,
            observeSoundVolume = observeSoundVolume,
        )

    @Test
    fun `購読前の初期状態はデフォルト速度を返す`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.0f), viewModel.uiState.value)
            verify(exactly = 1) { repository.voiceSpeed() }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みの速度をUiStateで返す`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.5f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            confirmVerified(repository)
        }

    @Test
    fun `速度を変更するとUiStateが更新される`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            coEvery { repository.saveVoiceSpeed(0.7f) } answers { speedFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onSpeedChanged(0.7f)

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(0.7f) }
            confirmVerified(repository)
        }

    @Test
    fun `スライダーの浮動小数点誤差を0点1刻みに丸めて保存する`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            coEvery { repository.saveVoiceSpeed(0.7f) } answers { speedFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onSpeedChanged(0.70000005f)

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(0.7f) }
            confirmVerified(repository)
        }

    @Test
    fun `試聴ボタンで保存済みの速度のまま音量付きで読み上げる`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(42)
            val finish = CompletableDeferred<Unit>()
            coEvery { speakText("試聴", volume = 42) } coAnswers { finish.await() }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onPreviewClicked("試聴")
            runCurrent()
            assertTrue(viewModel.uiState.value.isPreviewing)
            finish.complete(Unit)
            runCurrent()
            assertFalse(viewModel.uiState.value.isPreviewing)

            subscription.cancel()
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `試聴中に再度押すと停止する`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(42)
            var speakJob: Job? = null
            coEvery { speakText("試聴", volume = 42) } coAnswers {
                speakJob = currentCoroutineContext()[Job]
                CompletableDeferred<Unit>().await()
            }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onPreviewClicked("試聴")
            runCurrent()
            assertTrue(viewModel.uiState.value.isPreviewing)
            viewModel.onPreviewClicked("試聴")
            runCurrent()

            assertFalse(viewModel.uiState.value.isPreviewing)
            assertTrue(requireNotNull(speakJob).isCancelled)
            subscription.cancel()
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `音量が0以下なら試聴しない`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(0)
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onPreviewClicked("試聴")
            runCurrent()

            assertFalse(viewModel.uiState.value.isPreviewing)
            subscription.cancel()
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 0) { speakText("試聴", volume = 0) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `試聴の失敗は画面をクラッシュさせず試聴中を解除する`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(42)
            coEvery { speakText("試聴", volume = 42) } throws IllegalStateException("試聴失敗")
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onPreviewClicked("試聴")
            runCurrent()

            assertFalse(viewModel.uiState.value.isPreviewing)
            subscription.cancel()
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `試聴のキャンセルは通常の失敗として扱わない`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(42)
            val cancellation = CancellationException("キャンセル")
            var completionCause: Throwable? = null
            coEvery { speakText("試聴", volume = 42) } coAnswers {
                currentCoroutineContext()[Job]!!.invokeOnCompletion { completionCause = it }
                throw cancellation
            }
            val viewModel = createViewModel()

            viewModel.onPreviewClicked("試聴")
            runCurrent()

            assertEquals(cancellation, completionCause)
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `停止した古い試聴が遅れて終了しても新しい試聴状態を消さない`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            every { observeSoundVolume() } returns flowOf(42)
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { speakText("試聴", volume = 42) } coAnswers {
                if (++calls == 1) {
                    // キャンセルに協調しない外部処理が、停止と再開の後に終了する状況を再現する。
                    withContext(NonCancellable) { oldFinish.await() }
                } else {
                    newFinish.await()
                }
            }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onPreviewClicked("試聴")
            runCurrent()
            viewModel.onPreviewClicked("試聴")
            runCurrent()
            viewModel.onPreviewClicked("試聴")
            runCurrent()
            assertTrue(viewModel.uiState.value.isPreviewing)
            oldFinish.complete(Unit)
            runCurrent()
            assertTrue(viewModel.uiState.value.isPreviewing)
            newFinish.complete(Unit)
            runCurrent()
            assertFalse(viewModel.uiState.value.isPreviewing)

            subscription.cancel()
            verify(exactly = 1) { repository.voiceSpeed() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 2) { speakText("試聴", volume = 42) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }
}
