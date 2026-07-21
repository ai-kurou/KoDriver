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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsFlagRepository

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
        observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
    )

    @Test
    fun `フラグ情報を未取得の場合は uiState の raceFlags が null`() = runTest {
        every { repository.flagStream() } returns MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.UNKNOWN))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.UNKNOWN, state.raceFlags?.gamePhase)
        verify(exactly = 1) { repository.flagStream() }
        confirmVerified(repository)
    }

    @Test
    fun `フラグ情報を購読すると uiState に反映される`() = runTest {
        val flagsFlow = MutableStateFlow(sampleRaceFlags(gamePhase = SessionPhase.GARAGE))
        every { repository.flagStream() } returns flagsFlow
        val viewModel = createViewModel()

        flagsFlow.value = sampleRaceFlags(gamePhase = SessionPhase.GREEN_FLAG)
        val state = viewModel.uiState.first()

        assertEquals(SessionPhase.GREEN_FLAG, state.raceFlags?.gamePhase)
        verify(exactly = 1) { repository.flagStream() }
        confirmVerified(repository)
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
