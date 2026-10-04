package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutMyBestLapDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: Gt7Ps5MyBestLapPreferencesRepository = mockk()

    private val enabledRepository: ReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        Gt7Ps5ReadoutMyBestLapDetailViewModel(
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeEnabledStates = ObserveReadoutEnabledStatesUseCase(enabledRepository),
                    saveEnabledState = SaveReadoutEnabledStateUseCase(enabledRepository),
                ),
            readout =
                MyBestLapReadoutUseCases(
                    ObserveGt7Ps5MyBestLapReadoutTextUseCase(repository),
                    SaveGt7Ps5MyBestLapReadoutTextUseCase(repository),
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            val viewModel = createViewModel()

            assertEquals(Gt7Ps5ReadoutMyBestLapDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `onEnabledChangedを呼ぶとuiStateのenabledが更新される`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns enabledStatesFlow
            coEvery {
                enabledRepository.saveReadoutEnabledState(
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
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) {
                enabledRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled,
                    false,
                )
            }
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            coEvery { repository.saveReadoutText("更新{laptime}") } answers
                { textFlow.update { "更新{laptime}" } }
            val viewModel = createViewModel()
            assertEquals(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextChanged(" 更新{laptime} ")
            assertEquals("更新{laptime}", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            coVerify(exactly = 1) { repository.saveReadoutText("更新{laptime}") }
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `サンプルタイムに置換して開始音の後に試聴する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) } returns Unit
            coEvery { speakText("更新1分23秒456{unknown}", volume = 60) } returns Unit
            createViewModel().onReadoutTextPreviewClicked("更新{laptime}{unknown}")
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) }
            coVerify(exactly = 1) { speakText("更新1分23秒456{unknown}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root)
                speakText("更新1分23秒456{unknown}", volume = 60)
            }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            createViewModel().onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            coVerify(exactly = 0) { speakText("注意", volume = -1) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }
}
