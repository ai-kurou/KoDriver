package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class AceWindowsReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk()

    private val observeText: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val saveText: SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()

    private val highThresholdFlow = MutableStateFlow(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutTyreTemperatureDetailViewModel(
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeEnabledStates = ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(repository),
                    observeHighThreshold = ObserveAceWindowsTyreTemperatureHighThresholdUseCase(repository),
                    saveEnabledState = SaveAceWindowsTyreTemperatureEnabledStateUseCase(repository),
                    saveHighThreshold = SaveAceWindowsTyreTemperatureHighThresholdUseCase(repository),
                ),
            readout =
                TyreTemperatureReadoutUseCases(
                    observeText,
                    saveText,
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            stubReadout()
            val viewModel = createViewModel()

            assertEquals(AceWindowsReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            confirmVerified(repository)
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false) }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(Celsius(105))
            } answers { highThresholdFlow.update { Celsius(105) } }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onHighThresholdChanged(105)

            assertEquals(105, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(105)) }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値に戻る`() =
        runTest {
            highThresholdFlow.update { Celsius(105) }
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            } answers {
                highThresholdFlow.update { ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onHighThresholdReset()

            assertEquals(
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
                viewModel.uiState.first().highThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            }
            confirmVerified(repository)
        }

    private val textFlow = MutableStateFlow(ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)

    private fun stubReadout(available: Boolean = false) {
        every { observeText() } returns textFlow
        coEvery { checkAvailable() } returns available
    }

    private fun stubSettings() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
    }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            coEvery { saveText(" 注意 ") } answers { textFlow.update { "注意" } }
            val viewModel = createViewModel()
            assertEquals(
                ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                viewModel.uiState.first().overheatReadoutText,
            )
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onOverheatReadoutTextChanged(" 注意 ")
            assertEquals("注意", viewModel.uiState.first().overheatReadoutText)
            verify(exactly = 1) { observeText() }
            coVerify(exactly = 1) { saveText(" 注意 ") }
            confirmVerified(observeText, saveText)
        }

    @Test
    fun `空欄への変更を保存してUiStateに反映する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            coEvery { saveText("") } answers { textFlow.update { "" } }
            val viewModel = createViewModel()

            viewModel.onOverheatReadoutTextChanged("")

            assertEquals("", viewModel.uiState.first().overheatReadoutText)
            coVerify(exactly = 1) { saveText("") }
            confirmVerified(saveText)
        }

    @Test
    fun `渡された温度を置換して未知のトークンを維持し開始音の後に試聴する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            every { observeVolume() } returns flowOf(60)
            coEvery { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) } returns Unit
            coEvery { speakText("注意107℃{unknown}", volume = 60) } returns Unit
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("注意{celsius}℃{unknown}", 107)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 1) { speakText("注意107℃{unknown}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root)
                speakText("注意107℃{unknown}", volume = 60)
            }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            createViewModel().onOverheatReadoutTextPreviewClicked(" ", 100)
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = false)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onOverheatReadoutTextPreviewClicked("注意", 100)
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("注意", 100)
            volume.update { -1 }
            viewModel.onOverheatReadoutTextPreviewClicked("注意", 100)
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            every { observeVolume() } returns flowOf(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onOverheatReadoutTextPreviewClicked("注意{celsius}℃{unknown}", 107)
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意107℃{unknown}", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            every { observeVolume() } returns flowOf(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onOverheatReadoutTextPreviewClicked("注意{celsius}℃{unknown}", 107)
            viewModel.onOverheatReadoutTextPreviewClicked("注意{celsius}℃{unknown}", 107)
            pendingStartSound.complete(Unit)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意107℃{unknown}", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }
}
