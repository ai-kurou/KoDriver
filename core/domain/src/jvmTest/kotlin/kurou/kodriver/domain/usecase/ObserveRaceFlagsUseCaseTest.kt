package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveRaceFlagsUseCaseTest {

    @Test
    fun `invokeはリポジトリのflagStreamを返す`() = runBlocking {
        val expected = fakeRaceFlagsData(
            gamePhase = SessionPhase.GREEN_FLAG,
            yellowFlagState = SessionYellowFlagState.PIT_CLOSED,
            playerFlag = PrimaryFlag.BLUE,
        )
        val repo = FakeFlagRepository(stream = flowOf(expected))
        val useCase = ObserveRaceFlagsUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeFlagRepository(stream = flowOf())
        val useCase = ObserveRaceFlagsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeRaceFlagsData(gamePhase = SessionPhase.WARM_UP)
        val data2 = fakeRaceFlagsData(gamePhase = SessionPhase.GRID_WALK)
        val data3 = fakeRaceFlagsData(gamePhase = SessionPhase.FORMATION)
        val repo = FakeFlagRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveRaceFlagsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
