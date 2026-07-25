package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

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
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutPitTimingDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsPitTimingPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val virtualEnergyLapsFlow = MutableStateFlow(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)
    private val tyreWearLapsFlow = MutableStateFlow(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutPitTimingDetailViewModel(
        observeLmuWindowsPitTimingVirtualEnergyLaps = ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository),
        observeLmuWindowsPitTimingTyreWearLaps = ObserveLmuWindowsPitTimingTyreWearLapsUseCase(repository),
        saveLmuWindowsPitTimingVirtualEnergyLaps = SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository),
        saveLmuWindowsPitTimingTyreWearLaps = SaveLmuWindowsPitTimingTyreWearLapsUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態は両方とも3周のUiStateを返す`() = runTest {
        every { repository.observeVirtualEnergyLaps() } returns virtualEnergyLapsFlow
        every { repository.observeTyreWearLaps() } returns tyreWearLapsFlow
        val viewModel = createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT, uiState.virtualEnergyLaps)
        assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, uiState.tyreWearLaps)
        verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
        verify(exactly = 1) { repository.observeTyreWearLaps() }
        confirmVerified(repository)
    }

    @Test
    fun `onVirtualEnergyLapsChangedに5を渡すと保存されvirtualEnergyLapsが5になる`() = runTest {
        every { repository.observeVirtualEnergyLaps() } returns virtualEnergyLapsFlow
        every { repository.observeTyreWearLaps() } returns tyreWearLapsFlow
        coEvery { repository.saveVirtualEnergyLaps(5) } answers { virtualEnergyLapsFlow.update { 5 } }
        val viewModel = createViewModel()

        viewModel.onVirtualEnergyLapsChanged(5)

        assertEquals(5, viewModel.uiState.first().virtualEnergyLaps)
        verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
        verify(exactly = 1) { repository.observeTyreWearLaps() }
        coVerify(exactly = 1) { repository.saveVirtualEnergyLaps(5) }
        confirmVerified(repository)
    }

    @Test
    fun `onTyreWearLapsChangedに1を渡すと保存されtyreWearLapsが1になる`() = runTest {
        every { repository.observeVirtualEnergyLaps() } returns virtualEnergyLapsFlow
        every { repository.observeTyreWearLaps() } returns tyreWearLapsFlow
        coEvery { repository.saveTyreWearLaps(1) } answers { tyreWearLapsFlow.update { 1 } }
        val viewModel = createViewModel()

        viewModel.onTyreWearLapsChanged(1)

        assertEquals(1, viewModel.uiState.first().tyreWearLaps)
        verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
        verify(exactly = 1) { repository.observeTyreWearLaps() }
        coVerify(exactly = 1) { repository.saveTyreWearLaps(1) }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClickedを呼ぶと5周と0周のPitTimingWarningイベントが再生される`() {
        every { repository.observeVirtualEnergyLaps() } returns virtualEnergyLapsFlow
        every { repository.observeTyreWearLaps() } returns tyreWearLapsFlow
        every { ttsEngine.speak(SpeechEvent.PitTimingWarning(5), false) } returns Unit
        every { ttsEngine.speak(SpeechEvent.PitTimingWarning(0), true) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
        verify(exactly = 1) { repository.observeTyreWearLaps() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.PitTimingWarning(5), false) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.PitTimingWarning(0), true) }
        confirmVerified(repository, ttsEngine)
    }
}
