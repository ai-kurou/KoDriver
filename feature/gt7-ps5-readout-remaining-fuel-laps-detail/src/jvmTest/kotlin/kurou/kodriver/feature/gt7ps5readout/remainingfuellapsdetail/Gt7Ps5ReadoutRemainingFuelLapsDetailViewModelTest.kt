package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT)

    private val emptyTextFlow = MutableStateFlow(GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT)

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
            remainingFuelLapsUseCases =
                RemainingFuelLapsUseCases(
                    observeRemainingFuelLaps =
                        ObserveGt7Ps5RemainingFuelLapsUseCase(repository),
                    saveRemainingFuelLaps =
                        SaveGt7Ps5RemainingFuelLapsUseCase(repository),
                    observeReadoutEnabledStates =
                        ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState =
                        SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                ),
            readout =
                RemainingFuelLapsReadoutUseCases(
                    ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase(repository),
                    SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase(repository),
                    ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(repository),
                    SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(repository),
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            val viewModel = createViewModel()
            assertEquals(Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(), viewModel.uiState.value)

            assertEquals(
                Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                    remainingFuelLaps = 3,
                    enabled = true,
                    readoutText = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onRemainingFuelLapsChangedを呼ぶとuiStateのremainingFuelLapsが更新される`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(4)
            every { repository.observeRemainingFuelLaps() } returns thresholdFlow
            coEvery { repository.saveRemainingFuelLaps(3) } answers { thresholdFlow.update { 3 } }
            val viewModel = createViewModel()

            viewModel.onRemainingFuelLapsChanged(3)

            assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) { repository.saveRemainingFuelLaps(3) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶとremainingFuelLapsがデフォルト値3に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(4)
            every { repository.observeRemainingFuelLaps() } returns thresholdFlow
            coEvery { repository.saveRemainingFuelLaps(3) } answers { thresholdFlow.update { 3 } }
            val viewModel = createViewModel()

            viewModel.onResetRemainingFuelLaps()

            assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) { repository.saveRemainingFuelLaps(3) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.Gt7Ps5.id,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    private fun stubReadout(available: Boolean = true) {
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns
            enabledStatesFlow
        every { repository.observeReadoutText() } returns textFlow
        every { repository.observeEmptyReadoutText() } returns emptyTextFlow
        coEvery { checkAvailable() } returns available
    }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(4)
            coEvery { repository.saveReadoutText("残り{laps}周") } answers { textFlow.update { "残り{laps}周" } }
            val viewModel = createViewModel()
            assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{laps}周 ")
            assertEquals("残り{laps}周", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{laps}周") }
            confirmVerified(repository)
        }

    @Test
    fun `現在の閾値に置換して開始音の後に試聴する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(4)
            every { repository.observeRemainingFuelLaps() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) } returns Unit
            coEvery { speakText("残り4周", volume = 60) } returns Unit
            coEvery { speakText("残り5周", volume = 60) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)
            viewModel.onReadoutTextPreviewClicked("残り{laps}周")
            threshold.update { 5 }
            viewModel.onReadoutTextPreviewClicked("残り{laps}周")
            coVerify(exactly = 2) { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) }
            coVerify(exactly = 1) { speakText("残り4周", volume = 60) }
            coVerify(exactly = 1) { speakText("残り5周", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)
                speakText("残り4周", volume = 60)
                playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)
                speakText("残り5周", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(4)
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked(" ")
            viewModel.onEmptyReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubReadout(available = false)
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(4)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(4)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            verify(exactly = 4) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `燃料なし用の文言を保存して反映する`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            coEvery { repository.saveEmptyReadoutText("燃料切れ") } answers { emptyTextFlow.update { "燃料切れ" } }
            val viewModel = createViewModel()
            assertEquals("燃料残り1周未満", viewModel.uiState.first().emptyReadoutText)
            viewModel.onEmptyReadoutTextChanged(" 燃料切れ ")
            assertEquals("燃料切れ", viewModel.uiState.first().emptyReadoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            coVerify(exactly = 1) { repository.saveEmptyReadoutText("燃料切れ") }
            confirmVerified(repository)
        }

    @Test
    fun `燃料なし用の文言は置換せず開始音の後に試聴する`() =
        runTest {
            stubReadout()
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) } returns Unit
            coEvery { speakText("燃料なし{laps}", volume = 60) } returns Unit
            createViewModel().onEmptyReadoutTextPreviewClicked("燃料なし{laps}")
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root) }
            coVerify(exactly = 1) { speakText("燃料なし{laps}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)
                speakText("燃料なし{laps}", volume = 60)
            }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }
}
