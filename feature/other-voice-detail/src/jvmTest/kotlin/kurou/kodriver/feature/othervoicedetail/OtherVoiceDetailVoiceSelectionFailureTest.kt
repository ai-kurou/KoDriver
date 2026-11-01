package kurou.kodriver.feature.othervoicedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
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

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceDetailVoiceSelectionFailureTest {
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
    fun `音声選択の保存に失敗しても例外が伝播せず選択は更新されない`() =
        runTest {
            val saved = MutableStateFlow(VOICE_ID_UNSPECIFIED)
            every { voicePreferencesRepository.voiceId() } returns saved
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            coEvery { voicePreferencesRepository.saveVoiceId(voice.id) } throws IllegalStateException("保存失敗")
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onVoiceSelected(voice.id)
            runCurrent()

            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().selectedVoiceId)
            coVerify(exactly = 1) { voicePreferencesRepository.saveVoiceId(voice.id) }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            job.cancel()
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }

    @Test
    fun `音声選択の保存がキャンセルされても通常の失敗として扱わない`() =
        runTest {
            val saved = MutableStateFlow(VOICE_ID_UNSPECIFIED)
            every { voicePreferencesRepository.voiceId() } returns saved
            coEvery { voiceListRepository.availableVoices() } returns listOf(voice)
            coEvery { voicePreferencesRepository.saveVoiceId(voice.id) } throws CancellationException("キャンセル")
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onVoiceSelected(voice.id)
            runCurrent()

            assertEquals(VOICE_ID_UNSPECIFIED, viewModel.uiState.first().selectedVoiceId)
            coVerify(exactly = 1) { voicePreferencesRepository.saveVoiceId(voice.id) }
            verify(exactly = 1) { voicePreferencesRepository.voiceId() }
            coVerify(exactly = 1) { voiceListRepository.availableVoices() }
            job.cancel()
            confirmVerified(voicePreferencesRepository, voiceListRepository)
        }
}
