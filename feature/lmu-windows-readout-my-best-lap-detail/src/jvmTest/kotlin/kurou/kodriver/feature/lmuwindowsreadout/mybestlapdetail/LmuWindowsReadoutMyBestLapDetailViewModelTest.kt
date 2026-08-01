@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutMyBestLapDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsMyBestLapPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val voiceTypeFlow = MutableStateFlow(MyBestLapVoiceType.FORMAL)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutMyBestLapDetailViewModel(
            observeMyBestLapVoiceType = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository),
            saveMyBestLapVoiceType = SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態は voiceType=FORMAL の UiState を返す`() =
        runTest {
            every { repository.observeVoiceType() } returns voiceTypeFlow
            val viewModel = createViewModel()

            assertEquals(MyBestLapVoiceType.FORMAL, viewModel.uiState.first().voiceType)
            verify(exactly = 1) { repository.observeVoiceType() }
            confirmVerified(repository)
        }

    @Test
    fun `onVoiceTypeChanged に CASUAL を渡すと voiceType=CASUAL になる`() =
        runTest {
            every { repository.observeVoiceType() } returns voiceTypeFlow
            coEvery { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) } answers {
                voiceTypeFlow.update { MyBestLapVoiceType.CASUAL }
            }
            val viewModel = createViewModel()

            viewModel.onVoiceTypeChanged(MyBestLapVoiceType.CASUAL)

            assertEquals(MyBestLapVoiceType.CASUAL, viewModel.uiState.first().voiceType)
            verify(exactly = 1) { repository.observeVoiceType() }
            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClicked に FORMAL を渡すと MyBestLapFormal イベントが再生される`() {
        every { repository.observeVoiceType() } returns voiceTypeFlow
        every { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLapFormal, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(MyBestLapVoiceType.FORMAL)

        verify(exactly = 1) { repository.observeVoiceType() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLapFormal, false) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onPreviewClicked に CASUAL を渡すと MyBestLapCasual イベントが再生される`() {
        every { repository.observeVoiceType() } returns voiceTypeFlow
        every { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLapCasual, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(MyBestLapVoiceType.CASUAL)

        verify(exactly = 1) { repository.observeVoiceType() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLapCasual, false) }
        confirmVerified(repository, ttsEngine)
    }
}
