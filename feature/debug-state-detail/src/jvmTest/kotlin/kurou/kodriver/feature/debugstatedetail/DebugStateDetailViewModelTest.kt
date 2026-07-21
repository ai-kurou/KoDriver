package kurou.kodriver.feature.debugstatedetail

import io.mockk.MockKAnnotations
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
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var flagRepository: LmuWindowsFlagRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DebugStateDetailViewModel(
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
        observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
    )

    @Test
    fun `フラグ情報を未取得の場合は uiState の raceFlags が null`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.UNKNOWN, state.raceFlags?.gamePhase)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        confirmVerified(simulatorPreferencesRepository, flagRepository)
    }

    @Test
    fun `フラグ情報を購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        val flagsFlow = MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GARAGE))
        every { flagRepository.flagStream() } returns flagsFlow
        val viewModel = createViewModel()

        flagsFlow.update { sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG) }
        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.GREEN_FLAG, state.raceFlags?.gamePhase)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        confirmVerified(simulatorPreferencesRepository, flagRepository)
    }

    @Test
    fun `選択中シミュレータを購読すると uiState に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(Simulator.LmuWindows, state.selectedSimulator)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        confirmVerified(simulatorPreferencesRepository, flagRepository)
    }

    @Test
    fun `初期状態のcardOrderはデフォルト順序`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO), state.cardOrder)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        confirmVerified(simulatorPreferencesRepository, flagRepository)
    }

    @Test
    fun `moveCardで順序を入れ替えるとuiStateへ即座に反映される`() = runTest {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { flagRepository.flagStream() } returns
            MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        val viewModel = createViewModel()

        viewModel.moveCard(0, 1)
        val state = viewModel.uiState.first()

        assertEquals(listOf(DebugStateCardKey.FLAG_INFO, DebugStateCardKey.SIMULATOR), state.cardOrder)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        verify(exactly = 1) { flagRepository.flagStream() }
        confirmVerified(simulatorPreferencesRepository, flagRepository)
    }

    private fun sampleRaceFlags(gamePhase: SessionPhase) = LmuWindowsRaceFlagsData(
        gamePhase = gamePhase,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
        startLight = 0,
        numRedLights = 0,
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )
}
