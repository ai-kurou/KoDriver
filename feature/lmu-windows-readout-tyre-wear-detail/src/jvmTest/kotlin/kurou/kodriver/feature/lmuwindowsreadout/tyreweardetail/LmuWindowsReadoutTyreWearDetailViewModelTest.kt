package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreWearDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsTyreWearPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val playSpeechEvent: PlaySpeechEventUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow("残量{percent}%以下")

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
        LmuWindowsReadoutTyreWearDetailViewModel(
            tyreWearUseCases =
                TyreWearUseCases(
                    observeThresholdPercentage =
                        ObserveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
                    saveThresholdPercentage =
                        SaveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
                    observeReadoutEnabledStates =
                        ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState =
                        SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                    observeText = ObserveLmuWindowsTyreWearReadoutTextUseCase(repository),
                    saveText = SaveLmuWindowsTyreWearReadoutTextUseCase(repository),
                ),
            readout =
                TyreWearReadoutUseCases(
                    playSpeechEvent,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutTyreWearDetailUiState(
                    thresholdPercentage = 50,
                    enabled = true,
                    readoutText = "残量{percent}%以下",
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            confirmVerified(repository, readoutPreferencesRepository)
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
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値50に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(70)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
            val viewModel = createViewModel()

            viewModel.onThresholdReset()

            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    ReadoutItemKey.LmuWindows.TyreWear.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (ReadoutItemKey.LmuWindows.TyreWear.WarningReadout to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    ReadoutItemKey.LmuWindows.TyreWear.WarningReadout,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    private fun stubReadout(available: Boolean = true) {
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) } returns
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
            assertEquals("残量{percent}%以下", viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{percent}% ")
            assertEquals("残り{percent}%", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}%") }
            confirmVerified(repository)
        }

    @Test
    fun `現在の閾値と編集中の文言を解決したイベントで試聴する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.TyreWearWarning(50, "残り50%")) } returns Unit
            every { playSpeechEvent(SpeechEvent.TyreWearWarning(70, "残り70%")) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            threshold.update { 70 }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.TyreWearWarning(50, "残り50%")) }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.TyreWearWarning(70, "残り70%")) }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playSpeechEvent, observeVolume)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            createViewModel().onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.TyreWearWarning(50, " ")) }
            confirmVerified(observeVolume, playSpeechEvent)
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
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.TyreWearWarning(50, "注意")) }
            confirmVerified(observeVolume, playSpeechEvent)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.TyreWearWarning(50, "注意")) }
            confirmVerified(observeVolume, playSpeechEvent)
        }
}
