package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutPitTimingDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val readoutTextRepository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk()
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val tyreTextFlow = MutableStateFlow("タイヤ残り{laps}周")
    private val tyreImminentTextFlow = MutableStateFlow("タイヤ交換へ")
    private val textFlow = MutableStateFlow("残り{laps}周")
    private val imminentTextFlow = MutableStateFlow("必ずピットイン")

    private val virtualEnergyLapsFlow = MutableStateFlow(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)
    private val tyreWearLapsFlow = MutableStateFlow(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)
    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubRepository(available: Boolean = true) {
        coEvery { checkAvailable() } returns available
        every { readoutTextRepository.observeTyreWearReadoutText() } returns tyreTextFlow
        every { readoutTextRepository.observeTyreWearImminentReadoutText() } returns tyreImminentTextFlow
        every { readoutTextRepository.observeVirtualEnergyReadoutText() } returns textFlow
        every { readoutTextRepository.observeVirtualEnergyImminentReadoutText() } returns imminentTextFlow
        every { repository.observeVirtualEnergyLaps() } returns virtualEnergyLapsFlow
        every { repository.observeTyreWearLaps() } returns tyreWearLapsFlow
        every { repository.observeEnabledStates() } returns enabledStatesFlow
    }

    private fun createViewModel() =
        LmuWindowsReadoutPitTimingDetailViewModel(
            pitTimingUseCases =
                PitTimingUseCases(
                    observeVirtualEnergyLaps =
                        ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(
                            repository,
                        ),
                    observeTyreWearLaps = ObserveLmuWindowsPitTimingTyreWearLapsUseCase(repository),
                    observeEnabledStates = ObserveLmuWindowsPitTimingEnabledStatesUseCase(repository),
                    saveVirtualEnergyLaps = SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository),
                    saveTyreWearLaps = SaveLmuWindowsPitTimingTyreWearLapsUseCase(repository),
                    saveEnabledState = SaveLmuWindowsPitTimingEnabledStateUseCase(repository),
                ),
            readout =
                PitTimingReadoutUseCases(
                    ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(readoutTextRepository),
                    ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(readoutTextRepository),
                    SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(readoutTextRepository),
                    SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(readoutTextRepository),
                    ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(readoutTextRepository),
                    ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(readoutTextRepository),
                    SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(readoutTextRepository),
                    SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(readoutTextRepository),
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態は両方とも3周かつ有効のUiStateを返す`() =
        runTest {
            stubRepository()
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.first()

            assertEquals(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT, uiState.virtualEnergyLaps)
            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, uiState.tyreWearLaps)
            assertEquals(true, uiState.virtualEnergyEnabled)
            assertEquals(true, uiState.tyreWearEnabled)
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `onVirtualEnergyLapsChangedに5を渡すと保存されvirtualEnergyLapsが5になる`() =
        runTest {
            stubRepository()
            coEvery { repository.saveVirtualEnergyLaps(5) } answers { virtualEnergyLapsFlow.update { 5 } }
            val viewModel = createViewModel()

            viewModel.onVirtualEnergyLapsChanged(5)

            assertEquals(5, viewModel.uiState.first().virtualEnergyLaps)
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) { repository.saveVirtualEnergyLaps(5) }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `onTyreWearLapsChangedに1を渡すと保存されtyreWearLapsが1になる`() =
        runTest {
            stubRepository()
            coEvery { repository.saveTyreWearLaps(1) } answers { tyreWearLapsFlow.update { 1 } }
            val viewModel = createViewModel()

            viewModel.onTyreWearLapsChanged(1)

            assertEquals(1, viewModel.uiState.first().tyreWearLaps)
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) { repository.saveTyreWearLaps(1) }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `onVirtualEnergyEnabledChangedにfalseを渡すと保存されvirtualEnergyEnabledがfalseになる`() =
        runTest {
            stubRepository()
            coEvery { repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false) } answers {
                enabledStatesFlow.update { it + (LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false) }
            }
            val viewModel = createViewModel()

            viewModel.onVirtualEnergyEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().virtualEnergyEnabled)
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false)
            }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `onTyreWearEnabledChangedにfalseを渡すと保存されtyreWearEnabledがfalseになる`() =
        runTest {
            stubRepository()
            coEvery { repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.TyreWear, false) } answers {
                enabledStatesFlow.update { it + (LmuWindowsReadoutItemKey.PitTiming.TyreWear to false) }
            }
            val viewModel = createViewModel()

            viewModel.onTyreWearEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().tyreWearEnabled)
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.TyreWear, false)
            }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
            verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubRepository()
            coEvery { readoutTextRepository.saveVirtualEnergyReadoutText("残り{laps}周です") } answers {
                textFlow.update { "残り{laps}周です" }
            }
            coEvery { readoutTextRepository.saveVirtualEnergyImminentReadoutText("ピットへ") } answers {
                imminentTextFlow.update { "ピットへ" }
            }
            val viewModel = createViewModel()
            assertEquals("残り{laps}周", viewModel.uiState.first().virtualEnergyText)
            assertEquals("必ずピットイン", viewModel.uiState.first().virtualEnergyImminentText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onVirtualEnergyTextChanged("残り{laps}周です")
            viewModel.onVirtualEnergyImminentTextChanged("ピットへ")
            assertEquals("残り{laps}周です", viewModel.uiState.first().virtualEnergyText)
            assertEquals("ピットへ", viewModel.uiState.first().virtualEnergyImminentText)
            coVerify(exactly = 1) { readoutTextRepository.saveVirtualEnergyReadoutText("残り{laps}周です") }
            coVerify(exactly = 1) { readoutTextRepository.saveVirtualEnergyImminentReadoutText("ピットへ") }
            verifyRepositoryObservations()
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `通常は5周に置換し切迫時は入力のまま開始音の後に試聴する`() =
        runTest {
            stubRepository()
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) } returns Unit
            coEvery { speakText("残り5周", volume = 60) } returns Unit
            coEvery { speakText("必ず{laps}", volume = 60) } returns Unit
            val viewModel = createViewModel()
            viewModel.onVirtualEnergyTextPreviewClicked("残り{laps}周")
            viewModel.onVirtualEnergyImminentTextPreviewClicked("必ず{laps}")
            coVerify(exactly = 2) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 1) { speakText("残り5周", volume = 60) }
            coVerify(exactly = 1) { speakText("必ず{laps}", volume = 60) }
            coVerifyOrder {
                playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root)
                speakText("残り5周", volume = 60)
                playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root)
                speakText("必ず{laps}", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubRepository()
            val viewModel = createViewModel()
            viewModel.onVirtualEnergyTextPreviewClicked(" ")
            viewModel.onVirtualEnergyImminentTextPreviewClicked("")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubRepository(available = false)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onVirtualEnergyTextPreviewClicked("注意")
            viewModel.onVirtualEnergyImminentTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubRepository()
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onVirtualEnergyTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onVirtualEnergyImminentTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `タイヤ摩耗の文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubRepository()
            coEvery { readoutTextRepository.saveTyreWearReadoutText("残り{laps}周です") } answers {
                tyreTextFlow.update { "残り{laps}周です" }
            }
            coEvery { readoutTextRepository.saveTyreWearImminentReadoutText("ピットへ") } answers {
                tyreImminentTextFlow.update { "ピットへ" }
            }
            val viewModel = createViewModel()
            assertEquals("タイヤ残り{laps}周", viewModel.uiState.first().tyreWearText)
            assertEquals("タイヤ交換へ", viewModel.uiState.first().tyreWearImminentText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onTyreWearTextChanged("残り{laps}周です")
            viewModel.onTyreWearImminentTextChanged("ピットへ")
            assertEquals("残り{laps}周です", viewModel.uiState.first().tyreWearText)
            assertEquals("ピットへ", viewModel.uiState.first().tyreWearImminentText)
            coVerify(exactly = 1) { readoutTextRepository.saveTyreWearReadoutText("残り{laps}周です") }
            coVerify(exactly = 1) { readoutTextRepository.saveTyreWearImminentReadoutText("ピットへ") }
            verifyRepositoryObservations()
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `タイヤ摩耗の通常は5周に置換し切迫時は入力のまま開始音の後に試聴する`() =
        runTest {
            stubRepository()
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) } returns Unit
            coEvery { speakText("残り5周", volume = 60) } returns Unit
            coEvery { speakText("必ず{laps}", volume = 60) } returns Unit
            val viewModel = createViewModel()
            viewModel.onTyreWearTextPreviewClicked("残り{laps}周")
            viewModel.onTyreWearImminentTextPreviewClicked("必ず{laps}")
            coVerify(exactly = 2) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 1) { speakText("残り5周", volume = 60) }
            coVerify(exactly = 1) { speakText("必ず{laps}", volume = 60) }
            coVerifyOrder {
                playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root)
                speakText("残り5周", volume = 60)
                playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root)
                speakText("必ず{laps}", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    private fun verifyRepositoryObservations() {
        verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
        verify(exactly = 1) { repository.observeTyreWearLaps() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { readoutTextRepository.observeTyreWearReadoutText() }
        verify(exactly = 1) { readoutTextRepository.observeTyreWearImminentReadoutText() }
        verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyReadoutText() }
        verify(exactly = 1) { readoutTextRepository.observeVirtualEnergyImminentReadoutText() }
    }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            stubRepository()
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onVirtualEnergyTextPreviewClicked("残り{laps}周")
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 0) { speakText("残り5周", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            stubRepository()
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onVirtualEnergyTextPreviewClicked("残り{laps}周")
            viewModel.onVirtualEnergyTextPreviewClicked("残り{laps}周")
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(LmuWindowsReadoutItemKey.PitTiming.Root) }
            coVerify(exactly = 0) { speakText("残り5周", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }
}
