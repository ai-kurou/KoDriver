package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

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
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)

    private val highThresholdFlow = MutableStateFlow(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeOverheatReadoutText() } returns textFlow
        coEvery { checkAvailable() } returns false
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        Gt7Ps5ReadoutTyreTemperatureDetailViewModel(
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeEnabledStates = ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase(repository),
                    observeHighThreshold = ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(repository),
                    saveEnabledState = SaveGt7Ps5TyreTemperatureEnabledStateUseCase(repository),
                    saveHighThreshold = SaveGt7Ps5TyreTemperatureHighThresholdUseCase(repository),
                ),
            readout =
                TyreTemperatureReadoutUseCases(
                    ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(repository),
                    SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(repository),
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
            val viewModel = createViewModel()

            assertEquals(Gt7Ps5ReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            confirmVerified(repository)
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning to false) }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            }
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
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
            val viewModel = createViewModel()

            viewModel.onHighThresholdChanged(105)

            assertEquals(105, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(105)) }
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値に戻る`() =
        runTest {
            highThresholdFlow.update { Celsius(105) }
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            } answers {
                highThresholdFlow.update { GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onHighThresholdReset()

            assertEquals(
                GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
                viewModel.uiState.first().highThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            }
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            confirmVerified(repository)
        }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeOverheatReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(Celsius(100))
            coEvery { repository.saveOverheatReadoutText("温度{celsius}度") } answers
                { textFlow.update { "温度{celsius}度" } }
            val viewModel = createViewModel()
            assertEquals(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextChanged(" 温度{celsius}度 ")
            assertEquals("温度{celsius}度", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveOverheatReadoutText("温度{celsius}度") }
            verify(exactly = 1) { repository.observeEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `現在の閾値に置換して開始音の後に試聴する`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeOverheatReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(Celsius(100))
            every { repository.observeHighThresholdCelsius() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) } returns Unit
            coEvery { speakText("温度100度{wheel}", volume = 60) } returns Unit
            coEvery { speakText("温度107度{wheel}", volume = 60) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(100, viewModel.uiState.first().highThresholdCelsius)
            viewModel.onReadoutTextPreviewClicked("温度{celsius}度{wheel}")
            threshold.update { Celsius(107) }
            viewModel.onReadoutTextPreviewClicked("温度{celsius}度{wheel}")
            coVerify(exactly = 2) { playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) }
            coVerify(exactly = 1) { speakText("温度100度{wheel}", volume = 60) }
            coVerify(exactly = 1) { speakText("温度107度{wheel}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root)
                speakText("温度100度{wheel}", volume = 60)
                playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root)
                speakText("温度107度{wheel}", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeOverheatReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(Celsius(100))
            createViewModel().onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeOverheatReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(Celsius(100))
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeOverheatReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(Celsius(100))
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            coVerify(exactly = 0) { speakText("注意", volume = -1) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }
}
