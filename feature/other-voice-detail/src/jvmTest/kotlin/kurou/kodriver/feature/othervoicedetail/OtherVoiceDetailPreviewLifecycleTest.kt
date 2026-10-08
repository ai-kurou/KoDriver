package kurou.kodriver.feature.othervoicedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
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

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
class OtherVoiceDetailPreviewLifecycleTest {
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
    fun `古い試聴が正常終了しても新しい試聴状態を消さない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns flowOf(42)
            coEvery { speakText("試聴", volume = 42, voiceId = voice.id) } coAnswers {
                // キャンセルに協調しない外部処理が、新しい試聴開始後に終了する状況を再現する。
                withContext(NonCancellable) { oldFinish.await() }
            }
            coEvery { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) } coAnswers { newFinish.await() }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()
            viewModel.onPreviewClicked(VOICE_ID_UNSPECIFIED, "試聴")
            runCurrent()
            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().previewingVoiceId)
            oldFinish.complete(Unit)
            runCurrent()
            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().previewingVoiceId)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingVoiceId)
            subscription.cancel()
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = voice.id) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) }
            confirmVerified(voicePreferencesRepository, voiceListRepository, observeSoundVolume, speakText)
        }

    @Test
    fun `古い試聴が例外終了しても新しい試聴状態を消さない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns flowOf(42)
            coEvery { speakText("試聴", volume = 42, voiceId = voice.id) } coAnswers {
                // キャンセルに協調しない外部処理が、新しい試聴開始後に終了する状況を再現する。
                withContext(NonCancellable) { oldFinish.await() }
                throw IllegalStateException("古い試聴の失敗")
            }
            coEvery { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) } coAnswers { newFinish.await() }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()
            viewModel.onPreviewClicked(VOICE_ID_UNSPECIFIED, "試聴")
            runCurrent()
            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().previewingVoiceId)
            oldFinish.complete(Unit)
            runCurrent()
            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().previewingVoiceId)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingVoiceId)
            subscription.cancel()
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = voice.id) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) }
            confirmVerified(voicePreferencesRepository, voiceListRepository, observeSoundVolume, speakText)
        }

    @Test
    fun `古い試聴が音量0で終了しても新しい試聴状態を消さない`() =
        runTest {
            every { voicePreferencesRepository.voiceId() } returns flowOf(VOICE_ID_UNSPECIFIED)
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            val newFinish = CompletableDeferred<Unit>()
            val viewModel = createViewModel()
            every { observeSoundVolume() } returnsMany
                listOf(
                    // Flow の通常のキャンセルチェックを通さず、遅れて音量を返す外部処理を再現する。
                    object : Flow<Int> {
                        override suspend fun collect(collector: FlowCollector<Int>) {
                            viewModel.onPreviewClicked(VOICE_ID_UNSPECIFIED, "試聴")
                            collector.emit(0)
                        }
                    },
                    flowOf(42),
                )

            coEvery { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) } coAnswers { newFinish.await() }
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(voice.id, "試聴")
            runCurrent()
            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().previewingVoiceId)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingVoiceId)
            subscription.cancel()
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 0) { speakText("試聴", volume = 0, voiceId = voice.id) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED) }
            confirmVerified(voicePreferencesRepository, voiceListRepository, observeSoundVolume, speakText)
        }
}
