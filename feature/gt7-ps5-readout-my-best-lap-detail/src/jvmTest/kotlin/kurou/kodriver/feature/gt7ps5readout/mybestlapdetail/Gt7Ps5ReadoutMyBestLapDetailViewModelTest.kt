@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutMyBestLapDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: Gt7Ps5MyBestLapPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val voiceTypeFlow = MutableStateFlow(MyBestLapVoiceType.FORMAL)

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns
            enabledStatesFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        Gt7Ps5ReadoutMyBestLapDetailViewModel(
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeVoiceType = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository),
                    saveVoiceType = SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository),
                    observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                ),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態は voiceType=FORMAL の UiState を返す`() =
        runTest {
            every { repository.observeVoiceType() } returns voiceTypeFlow
            val viewModel = createViewModel()

            assertEquals(MyBestLapVoiceType.FORMAL, viewModel.uiState.first().voiceType)
            assertEquals(true, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeVoiceType() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            confirmVerified(repository, readoutPreferencesRepository)
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
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { repository.observeVoiceType() } returns voiceTypeFlow
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled to false) }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeVoiceType() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onPreviewClicked に FORMAL を渡すと MyBestLapFormal イベントが再生される`() {
        every { repository.observeVoiceType() } returns voiceTypeFlow
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(MyBestLapVoiceType.FORMAL)

        verify(exactly = 1) { repository.observeVoiceType() }
        verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
        confirmVerified(repository, readoutPreferencesRepository, ttsEngine)
    }

    @Test
    fun `onPreviewClicked に CASUAL を渡すと MyBestLapCasual イベントが再生される`() {
        every { repository.observeVoiceType() } returns voiceTypeFlow
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapCasual, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(MyBestLapVoiceType.CASUAL)

        verify(exactly = 1) { repository.observeVoiceType() }
        verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapCasual, false) }
        confirmVerified(repository, readoutPreferencesRepository, ttsEngine)
    }
}
