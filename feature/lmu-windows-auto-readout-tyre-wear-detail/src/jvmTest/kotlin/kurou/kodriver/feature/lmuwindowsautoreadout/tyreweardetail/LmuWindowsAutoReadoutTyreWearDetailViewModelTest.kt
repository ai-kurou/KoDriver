package kurou.kodriver.feature.lmuwindowsautoreadout.tyreweardetail

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
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val DEFAULT_THRESHOLD = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutTyreWearDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsTyreWearPreferencesRepository = mockk()

    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository = mockk()

    private val stopSpeech: StopSpeechUseCase = mockk()
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
        LmuWindowsAutoReadoutTyreWearDetailViewModel(
            tyreWearUseCases =
                TyreWearUseCases(
                    observeThresholdPercentage =
                        ObserveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
                    saveThresholdPercentage =
                        SaveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
                    observeAutoReadoutEnabledStates =
                        ObserveAutoReadoutEnabledStatesUseCase(autoReadoutPreferencesRepository),
                    saveAutoReadoutEnabledState =
                        SaveAutoReadoutEnabledStateUseCase(autoReadoutPreferencesRepository),
                    observeText = ObserveLmuWindowsTyreWearReadoutTextUseCase(repository),
                    saveText = SaveLmuWindowsTyreWearReadoutTextUseCase(repository),
                ),
            readout =
                TyreWearReadoutUseCases(
                    playSpeechEvent,
                    stopSpeech,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(DEFAULT_THRESHOLD)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsAutoReadoutTyreWearDetailUiState(
                    thresholdPercentage = DEFAULT_THRESHOLD,
                    enabled = true,
                    readoutText = "残量{percent}%以下",
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(repository, autoReadoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(DEFAULT_THRESHOLD)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(50)

            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(repository, autoReadoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow = MutableStateFlow(70)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(DEFAULT_THRESHOLD) } answers {
                thresholdFlow.update { DEFAULT_THRESHOLD }
            }
            val viewModel = createViewModel()

            viewModel.onThresholdReset()

            assertEquals(
                DEFAULT_THRESHOLD,
                viewModel.uiState.first().thresholdPercentage,
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(DEFAULT_THRESHOLD) }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(repository, autoReadoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            coEvery {
                autoReadoutPreferencesRepository.saveAutoReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (LmuWindowsReadoutItemKey.TyreWear.WarningReadout to false)
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
                    LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
                    false,
                )
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(repository, autoReadoutPreferencesRepository, stopSpeech)
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
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(DEFAULT_THRESHOLD)
            coEvery { repository.saveReadoutText("残り{percent}%") } answers { textFlow.update { "残り{percent}%" } }
            val viewModel = createViewModel()
            assertEquals("残量{percent}%以下", viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{percent}% ")
            assertEquals("残り{percent}%", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}%") }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(repository, stopSpeech)
        }

    @Test
    fun `現在の閾値と編集中の文言を解決したイベントで試聴する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(DEFAULT_THRESHOLD)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "残り${DEFAULT_THRESHOLD}%",
                    ),
                )
            } returns Unit
            every { playSpeechEvent(LmuWindowsTyreWearWarning(70, "残り70%")) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(
                DEFAULT_THRESHOLD,
                viewModel.uiState.first().thresholdPercentage,
            )
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            threshold.update { 70 }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            verify(exactly = 1) {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "残り${DEFAULT_THRESHOLD}%",
                    ),
                )
            }
            verify(exactly = 1) { playSpeechEvent(LmuWindowsTyreWearWarning(70, "残り70%")) }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable, stopSpeech)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(DEFAULT_THRESHOLD)
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        " ",
                    ),
                )
            }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubReadout(available = false)
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(DEFAULT_THRESHOLD)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "注意",
                    ),
                )
            }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubReadout()
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(DEFAULT_THRESHOLD)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            verify(exactly = 0) {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "注意",
                    ),
                )
            }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `ペインを離れると開始した試聴を一度だけ停止する`() =
        runTest {
            stubReadout()
            val threshold = MutableStateFlow(DEFAULT_THRESHOLD)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "残り${DEFAULT_THRESHOLD}%",
                    ),
                )
            } returns Unit
            every { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            assertEquals(
                DEFAULT_THRESHOLD,
                viewModel.uiState.first().thresholdPercentage,
            )
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            verify(exactly = 1) {
                playSpeechEvent(
                    LmuWindowsTyreWearWarning(
                        DEFAULT_THRESHOLD,
                        "残り${DEFAULT_THRESHOLD}%",
                    ),
                )
            }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            viewModel.onPreviewStopped()
            viewModel.onPreviewStopped()
            verify(exactly = 1) { stopSpeech(LmuWindowsReadoutItemKey.TyreWear.Root) }
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            }
            confirmVerified(
                repository,
                autoReadoutPreferencesRepository,
                playSpeechEvent,
                observeVolume,
                checkAvailable,
                stopSpeech,
            )
        }
}
