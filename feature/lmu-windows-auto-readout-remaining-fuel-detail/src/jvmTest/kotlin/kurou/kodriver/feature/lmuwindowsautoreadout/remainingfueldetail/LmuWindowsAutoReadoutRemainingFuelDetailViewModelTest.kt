package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
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
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutRemainingFuelDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk()

    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow("燃料残り{percent}%以下")

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
        LmuWindowsAutoReadoutRemainingFuelDetailViewModel(
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeThresholdPercentage =
                        ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(repository),
                    saveThresholdPercentage =
                        SaveLmuWindowsRemainingFuelThresholdPercentageUseCase(repository),
                    observeAutoReadoutEnabledStates =
                        ObserveAutoReadoutEnabledStatesUseCase(autoReadoutPreferencesRepository),
                    saveAutoReadoutEnabledState =
                        SaveAutoReadoutEnabledStateUseCase(autoReadoutPreferencesRepository),
                ),
            readout =
                RemainingFuelReadoutUseCases(
                    ObserveLmuWindowsRemainingFuelReadoutTextUseCase(repository),
                    SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository),
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
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsAutoReadoutRemainingFuelDetailUiState(
                    thresholdPercentage = 30,
                    enabled = true,
                    readoutText = "燃料残り{percent}%以下",
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            confirmVerified(repository, autoReadoutPreferencesRepository)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(30)

            assertEquals(30, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, autoReadoutPreferencesRepository)
        }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
            val viewModel = createViewModel()

            viewModel.onThresholdReset()

            assertEquals(30, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, autoReadoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            coEvery {
                autoReadoutPreferencesRepository.saveAutoReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            coVerify(exactly = 1) {
                autoReadoutPreferencesRepository.saveAutoReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout,
                    false,
                )
            }
            confirmVerified(repository, autoReadoutPreferencesRepository)
        }

    private fun stubReadout(available: Boolean = true) {
        every { autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id) } returns
            enabledStatesFlow
        every { repository.observeReadoutText() } returns textFlow
        coEvery { checkAvailable() } returns available
    }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            coEvery { repository.saveReadoutText("残り{percent}%") } answers { textFlow.update { "残り{percent}%" } }
            val viewModel = createViewModel()
            assertEquals("燃料残り{percent}%以下", viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{percent}% ")
            assertEquals("残り{percent}%", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}%") }
            confirmVerified(repository)
        }

    @Test
    fun `現在の閾値に置換して開始音の後に試聴する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root) } returns Unit
            coEvery { speakText("残り50%", volume = 60) } returns Unit
            coEvery { speakText("残り70%", volume = 60) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            threshold.update { 70 }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            coVerify(exactly = 2) { playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 1) { speakText("残り50%", volume = 60) }
            coVerify(exactly = 1) { speakText("残り70%", volume = 60) }
            coVerifyOrder {
                playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root)
                speakText("残り50%", volume = 60)
                playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root)
                speakText("残り70%", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubReadout(available = false)
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(LmuWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("残り50%", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }
}
