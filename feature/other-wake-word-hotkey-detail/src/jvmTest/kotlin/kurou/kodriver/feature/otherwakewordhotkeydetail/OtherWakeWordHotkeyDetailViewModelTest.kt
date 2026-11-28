package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import kurou.kodriver.domain.speechrecognition.usecase.ObserveSpeechRecognitionEnabledUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyUseCase
import kurou.kodriver.domain.speechrecognition.usecase.RetryWakeWordHotkeyRegistrationUseCase
import kurou.kodriver.domain.speechrecognition.usecase.SaveWakeWordHotkeyUseCase
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherWakeWordHotkeyDetailViewModelTest {
    private val preferences: WakeWordHotkeyPreferencesRepository = mockk()
    private val hotkeyRepository: WakeWordHotkeyRepository = mockk()
    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()
    private val enabledRepository: SpeechRecognitionEnabledRepository = mockk()
    private val speechRecognitionEnabled = MutableStateFlow(false)
    private val dispatcher = UnconfinedTestDispatcher()
    private val hotkey = MutableStateFlow(WakeWordHotkey())
    private val registration = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Unsupported)
    private val lmuSets = MutableStateFlow(emptyList<ManualReadoutSet>())
    private val gt7Sets = MutableStateFlow(emptyList<ManualReadoutSet>())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { preferences.observeWakeWordHotkey() } returns hotkey
        every { enabledRepository.enabled } returns speechRecognitionEnabled
        every { hotkeyRepository.registrationStateStream() } returns registration
        every { lmuRepository.observeSets() } returns lmuSets
        every { gt7Repository.observeSets() } returns gt7Sets
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun withViewModel(block: suspend TestScope.(OtherWakeWordHotkeyDetailViewModel) -> Unit) =
        runTest(dispatcher) {
            val viewModel =
                OtherWakeWordHotkeyDetailViewModel(
                    ObserveWakeWordHotkeyUseCase(preferences),
                    SaveWakeWordHotkeyUseCase(preferences),
                    ObserveWakeWordHotkeyRegistrationStateUseCase(hotkeyRepository),
                    RetryWakeWordHotkeyRegistrationUseCase(hotkeyRepository),
                    mapOf(
                        Simulator.LmuWindows.id to ObserveManualReadoutSetsUseCase(lmuRepository),
                        Simulator.Gt7Ps5.id to ObserveManualReadoutSetsUseCase(gt7Repository),
                    ),
                    ObserveSpeechRecognitionEnabledUseCase(enabledRepository),
                )
            backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                block(viewModel)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `保存済みの割り当てと登録状態を表示する`() =
        withViewModel { viewModel ->
            hotkey.value = WakeWordHotkey(0x41, ManualReadoutHotkeyModifiers.ALT_SHIFT)
            registration.value = HotkeyRegistrationState.Registered

            assertEquals(
                OtherWakeWordHotkeyDetailUiState(
                    hotkeyVirtualKeyCode = 0x41,
                    hotkeyModifiers = ManualReadoutHotkeyModifiers.ALT_SHIFT,
                    hotkeyRegistrationState = HotkeyRegistrationState.Registered,
                ),
                viewModel.uiState.first(),
            )
        }

    @Test
    fun `割り当て済みで音声認識がOFFの間だけ注意書きを出す`() =
        withViewModel { viewModel ->
            assertEquals(false, viewModel.uiState.first().showSpeechRecognitionOffNotice)

            hotkey.value = WakeWordHotkey(0x41)
            assertEquals(true, viewModel.uiState.first().showSpeechRecognitionOffNotice)

            speechRecognitionEnabled.value = true
            assertEquals(false, viewModel.uiState.first().showSpeechRecognitionOffNotice)

            verify(exactly = 1) { enabledRepository.enabled }
            confirmVerified(enabledRepository)
        }

    @Test
    fun `キーの割り当てと修飾キーの変更と解除を保存する`() =
        withViewModel { viewModel ->
            val assigned = WakeWordHotkey(0x41)
            val modified = WakeWordHotkey(0x41, ManualReadoutHotkeyModifiers.CTRL_SHIFT)
            val cleared = modified.copy(virtualKeyCode = null)
            listOf(assigned, modified, cleared).forEach { value ->
                coEvery { preferences.saveWakeWordHotkey(value) } answers { hotkey.value = value }
            }

            viewModel.assignHotkey(0x41)
            viewModel.selectHotkeyModifiers(ManualReadoutHotkeyModifiers.CTRL_SHIFT)
            viewModel.clearHotkey()

            assertEquals(null, viewModel.uiState.first().hotkeyVirtualKeyCode)
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(assigned) }
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(modified) }
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(cleared) }
            verify(exactly = 4) { preferences.observeWakeWordHotkey() }
            confirmVerified(preferences)
        }

    @Test
    fun `対応していないキーは保存しない`() =
        withViewModel { viewModel ->
            viewModel.assignHotkey(0x70)

            verify(exactly = 1) { preferences.observeWakeWordHotkey() }
            confirmVerified(preferences)
        }

    @Test
    fun `手動読み上げセットと同じ組み合わせは保存せず重複を表示し同じ値の再選択で解除する`() =
        withViewModel { viewModel ->
            lmuSets.value =
                listOf(
                    ManualReadoutSet("first"),
                    ManualReadoutSet("second", hotkeyVirtualKeyCode = 0x41),
                )

            viewModel.assignHotkey(0x41)

            assertEquals(
                OtherWakeWordHotkeyConflict(Simulator.LmuWindows.id, 2),
                viewModel.uiState.first().hotkeyConflict,
            )
            assertEquals(null, viewModel.uiState.first().hotkeyVirtualKeyCode)

            viewModel.selectHotkeyModifiers(ManualReadoutHotkeyModifiers.CTRL_ALT)

            assertEquals(null, viewModel.uiState.first().hotkeyConflict)
            verify(exactly = 3) { preferences.observeWakeWordHotkey() }
            verify(exactly = 1) { lmuRepository.observeSets() }
            confirmVerified(preferences, lmuRepository, gt7Repository)
        }

    @Test
    fun `修飾キーの変更で別シミュレーターのセットと重複する場合は保存しない`() =
        withViewModel { viewModel ->
            hotkey.value = WakeWordHotkey(0x41)
            gt7Sets.value =
                listOf(
                    ManualReadoutSet(
                        "gt7",
                        hotkeyVirtualKeyCode = 0x41,
                        hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_SHIFT,
                        name = "GT7用",
                        number = 5,
                    ),
                )

            viewModel.selectHotkeyModifiers(ManualReadoutHotkeyModifiers.CTRL_SHIFT)

            assertEquals(
                OtherWakeWordHotkeyConflict(Simulator.Gt7Ps5.id, 5, "GT7用"),
                viewModel.uiState.first().hotkeyConflict,
            )
            assertEquals(ManualReadoutHotkeyModifiers.CTRL_ALT, viewModel.uiState.first().hotkeyModifiers)
            verify(exactly = 2) { preferences.observeWakeWordHotkey() }
            verify(exactly = 1) { lmuRepository.observeSets() }
            verify(exactly = 1) { gt7Repository.observeSets() }
            confirmVerified(preferences, lmuRepository, gt7Repository)
        }

    @Test
    fun `手動読み上げセットとキーか修飾キーが異なる場合は保存する`() =
        withViewModel { viewModel ->
            val assigned = WakeWordHotkey(0x41)
            lmuSets.value =
                listOf(
                    ManualReadoutSet(
                        "lmu",
                        hotkeyVirtualKeyCode = 0x41,
                        hotkeyModifiers = ManualReadoutHotkeyModifiers.ALT_SHIFT,
                    ),
                    ManualReadoutSet("other-key", hotkeyVirtualKeyCode = 0x42),
                )
            coEvery { preferences.saveWakeWordHotkey(assigned) } answers { hotkey.value = assigned }

            viewModel.assignHotkey(0x41)

            assertEquals(null, viewModel.uiState.first().hotkeyConflict)
            assertEquals(0x41, viewModel.uiState.first().hotkeyVirtualKeyCode)
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(assigned) }
            verify(exactly = 2) { preferences.observeWakeWordHotkey() }
            verify(exactly = 1) { lmuRepository.observeSets() }
            verify(exactly = 1) { gt7Repository.observeSets() }
            confirmVerified(preferences, lmuRepository, gt7Repository)
        }

    @Test
    fun `再試行を委譲する`() =
        withViewModel { viewModel ->
            coEvery { hotkeyRepository.retryRegistration() } returns Unit

            viewModel.onRetryHotkeyRegistration()

            coVerify(exactly = 1) { hotkeyRepository.retryRegistration() }
            verify(exactly = 1) { hotkeyRepository.registrationStateStream() }
            confirmVerified(hotkeyRepository)
        }

    @Test
    fun `保存と再試行の失敗はViewModel内で処理して状態を保つ`() =
        withViewModel { viewModel ->
            coEvery { preferences.saveWakeWordHotkey(WakeWordHotkey(0x41)) } throws IOException("save")
            coEvery { hotkeyRepository.retryRegistration() } throws IOException("retry")

            viewModel.assignHotkey(0x41)
            viewModel.onRetryHotkeyRegistration()

            assertEquals(null, viewModel.uiState.first().hotkeyVirtualKeyCode)
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(WakeWordHotkey(0x41)) }
            coVerify(exactly = 1) { hotkeyRepository.retryRegistration() }
            verify(exactly = 2) { preferences.observeWakeWordHotkey() }
            verify(exactly = 1) { hotkeyRepository.registrationStateStream() }
            confirmVerified(preferences, hotkeyRepository)
        }
}
