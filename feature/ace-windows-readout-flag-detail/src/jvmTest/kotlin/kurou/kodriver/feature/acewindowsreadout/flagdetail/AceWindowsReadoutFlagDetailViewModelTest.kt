package kurou.kodriver.feature.acewindowsreadout.flagdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutFlagDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: AceWindowsFlagPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutFlagDetailViewModel(
        observeFlagEnabledStates = ObserveAceWindowsFlagEnabledStatesUseCase(repository),
        saveFlagEnabledState = SaveAceWindowsFlagEnabledStateUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() =
        runTest {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        FlagReadoutItem.entries.forEach { item ->
            assertEquals(true, state.enabledStates[item.key])
        }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() =
        runTest {
        val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeFlagEnabledStates() } returns statesFlow
        coEvery { repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, false) } answers {
            statesFlow.update { it + (ReadoutItemKey.AceWindows.Flag.BlueFlag to false) }
        }
        val viewModel = createViewModel()

        viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

        assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.AceWindows.Flag.BlueFlag])
        coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClicked を呼ぶと各フラグに対応する SpeechEvent が再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        val eventByItem =
            mapOf(
            FlagReadoutItem.WhiteFlag to SpeechEvent.AceWindowsWhiteFlag,
            FlagReadoutItem.GreenFlag to SpeechEvent.AceWindowsGreenFlag,
            FlagReadoutItem.RedFlag to SpeechEvent.AceWindowsRedFlag,
            FlagReadoutItem.BlueFlag to SpeechEvent.AceWindowsBlueFlag,
            FlagReadoutItem.YellowFlag to SpeechEvent.AceWindowsYellowFlag,
            FlagReadoutItem.BlackFlag to SpeechEvent.AceWindowsBlackFlag,
            FlagReadoutItem.BlackWhiteFlag to SpeechEvent.AceWindowsBlackWhiteFlag,
            FlagReadoutItem.CheckeredFlag to SpeechEvent.AceWindowsCheckeredFlag,
            FlagReadoutItem.OrangeCircleFlag to SpeechEvent.AceWindowsOrangeCircleFlag,
            FlagReadoutItem.RedYellowStripesFlag to SpeechEvent.AceWindowsRedYellowStripesFlag,
        )
        assertEquals(FlagReadoutItem.entries.toSet(), eventByItem.keys)
        eventByItem.forEach { (_, event) -> every { ttsEngine.speak(event, false) } returns Unit }
        val viewModel = createViewModel()

        eventByItem.forEach { (item, _) -> viewModel.onPreviewClicked(item) }

        eventByItem.forEach { (_, event) -> verify(exactly = 1) { ttsEngine.speak(event, false) } }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(ttsEngine, repository)
    }
}
