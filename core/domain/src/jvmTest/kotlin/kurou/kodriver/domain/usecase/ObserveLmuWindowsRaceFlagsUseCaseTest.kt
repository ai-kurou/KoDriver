package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsRaceFlagsUseCaseTest {

    @MockK
    private lateinit var repo: LmuWindowsFlagRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのflagStreamを返す`() =
        runBlocking {
        val expected =
            fakeRaceFlagsData(
            gamePhase = SessionPhase.GREEN_FLAG,
            yellowFlagState = SessionYellowFlagState.PIT_CLOSED,
            playerFlag = PrimaryFlag.BLUE,
        )
        every { repo.flagStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsRaceFlagsUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() =
        runBlocking {
        every { repo.flagStream() } returns flowOf()
        val useCase = ObserveLmuWindowsRaceFlagsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runBlocking {
        val data1 = fakeRaceFlagsData(gamePhase = SessionPhase.WARM_UP)
        val data2 = fakeRaceFlagsData(gamePhase = SessionPhase.GRID_WALK)
        val data3 = fakeRaceFlagsData(gamePhase = SessionPhase.FORMATION)
        every { repo.flagStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsRaceFlagsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }
}
