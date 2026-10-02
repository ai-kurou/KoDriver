@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othervoicedetail

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.repository.VoiceListRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.usecase.GetAvailableVoicesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoiceUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val voiceListRepository: VoiceListRepository = mockk()
    private val voicePreferencesRepository: VoicePreferencesRepository = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val observeSoundVolume: ObserveSoundVolumeUseCase = mockk()
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVoiceDetailViewModel(
            GetAvailableVoicesUseCase(voiceListRepository),
            ObserveVoiceUseCase(voicePreferencesRepository),
            SaveVoiceUseCase(voicePreferencesRepository),
            speakText,
            observeSoundVolume,
        )

    @Test
    fun `購読すると取得中から音声一覧と保存済み選択を反映する`() =
        runTest {
            val result = CompletableDeferred<List<TextToSpeechVoice>>()
            every { voicePreferencesRepository.voiceId() } returns MutableStateFlow(voice.id)
            coEvery { voiceListRepository.availableVoices() } coAnswers { result.await() }
            val viewModel = createViewModel()
            assertTrue(viewModel.uiState.first().isLoading)
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            assertTrue(viewModel.uiState.first().isLoading)
            assertFalse(viewModel.uiState.first().savedVoiceMissing)

            result.complete(listOf(voice))
            runCurrent()

            assertEquals(
                OtherVoiceDetailUiState(voices = listOf(voice), selectedVoiceId = voice.id, isLoading = false),
                viewModel.uiState.first { !it.isLoading },
            )
            job.cancel()
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }

    @Test
    fun `保存済みIDが不在の場合は保存値を維持し不在を通知する`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns MutableStateFlow("missing")
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            val viewModel = createViewModel()
            val state = viewModel.uiState.first { !it.isLoading }

            assertEquals("missing", state.selectedVoiceId)
            assertTrue(state.savedVoiceMissing)
            coVerify(exactly = 0) { voicePreferencesRepository.saveVoiceId(VOICE_ID_UNSPECIFIED) }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }

    @Test
    fun `システム既定の場合は空の一覧でも保存済み不在にしない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns MutableStateFlow(VOICE_ID_UNSPECIFIED)
            coEvery { voiceListRepository.availableVoices() } returns emptyList()
            val state = createViewModel().uiState.first { !it.isLoading }

            assertEquals(OtherVoiceDetailUiState(isLoading = false), state)
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }

    @Test
    fun `音声選択は即時保存し設定監視から選択を更新する`() =
        runTest {
            val saved = MutableStateFlow(VOICE_ID_UNSPECIFIED)
            every { voicePreferencesRepository.voiceId() } returns saved
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            coEvery { voicePreferencesRepository.saveVoiceId(voice.id) } answers { saved.update { voice.id } }
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onVoiceSelected(voice.id)
            runCurrent()

            assertEquals(voice.id, viewModel.uiState.first().selectedVoiceId)
            coVerify(exactly = 1) { voicePreferencesRepository.saveVoiceId(voice.id) }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            job.cancel()
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }

    @Test
    fun `再読み込みすると空の一覧から再取得して不在状態を解消する`() =
        runTest {
            val retryResult = CompletableDeferred<List<TextToSpeechVoice>>()
            every { voicePreferencesRepository.voiceId() } returns MutableStateFlow(voice.id)
            var calls = 0
            coEvery { voiceListRepository.availableVoices() } coAnswers {
                if (calls++ == 0) emptyList() else retryResult.await()
            }
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            assertTrue(viewModel.uiState.first { !it.isLoading }.savedVoiceMissing)

            viewModel.onRetryClicked()
            runCurrent()
            assertTrue(viewModel.uiState.first().isLoading)
            retryResult.complete(listOf(voice))
            runCurrent()

            assertEquals(listOf(voice), viewModel.uiState.first { !it.isLoading }.voices)
            assertFalse(viewModel.uiState.first().savedVoiceMissing)
            coVerify(exactly = 2) { voiceListRepository.availableVoices() }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            job.cancel()
            confirmVerified(voiceListRepository, voicePreferencesRepository)
        }

    @Test
    fun `一覧の取得に失敗しても取得完了の空の一覧になり再読み込みで復帰できる`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns MutableStateFlow(VOICE_ID_UNSPECIFIED)
            var calls = 0
            coEvery { voiceListRepository.availableVoices() } coAnswers {
                if (calls++ == 0) throw IllegalStateException("取得失敗") else listOf(voice)
            }
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            val failed = viewModel.uiState.first { !it.isLoading }
            assertEquals(emptyList(), failed.voices)

            viewModel.onRetryClicked()
            runCurrent()

            assertEquals(listOf(voice), viewModel.uiState.first { !it.isLoading && it.voices.isNotEmpty() }.voices)
            coVerify(exactly = 2) { voiceListRepository.availableVoices() }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            job.cancel()
            confirmVerified(voiceListRepository, voicePreferencesRepository)
        }

    @Test
    fun `音量が正なら試聴する`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            every { observeSoundVolume() } returns flowOf(42)
            coEvery { speakText("試聴", volume = 42, voiceId = voice.id) } returns Unit
            val viewModel = createViewModel()

            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()

            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = voice.id) }
            confirmVerified(voicePreferencesRepository, observeSoundVolume, speakText)
        }

    @Test
    fun `試聴の失敗は画面をクラッシュさせない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            every { observeSoundVolume() } returns flowOf(42)
            coEvery { speakText("試聴", volume = 42, voiceId = voice.id) } throws IllegalStateException("試聴失敗")
            val viewModel = createViewModel()

            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()

            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = voice.id) }
            confirmVerified(voicePreferencesRepository, observeSoundVolume, speakText)
        }

    @Test
    fun `試聴のキャンセルは通常の失敗として扱わない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            every { observeSoundVolume() } returns flowOf(42)
            val cancellation = CancellationException("キャンセル")
            var completionCause: Throwable? = null
            coEvery { speakText("試聴", volume = 42, voiceId = voice.id) } coAnswers {
                currentCoroutineContext()[Job]!!.invokeOnCompletion { completionCause = it }
                throw cancellation
            }
            val viewModel = createViewModel()

            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()

            assertSame(cancellation, completionCause)
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = voice.id) }
            confirmVerified(voicePreferencesRepository, observeSoundVolume, speakText)
        }

    @Test
    fun `音量が0以下なら試聴しない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            every { observeSoundVolume() } returns flowOf(0)
            val viewModel = createViewModel()

            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()
            every { observeSoundVolume() } returns flowOf(-1)
            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()

            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 0) { speakText("試聴", volume = 0, voiceId = voice.id) }
            confirmVerified(voicePreferencesRepository, observeSoundVolume, speakText)
        }
}
