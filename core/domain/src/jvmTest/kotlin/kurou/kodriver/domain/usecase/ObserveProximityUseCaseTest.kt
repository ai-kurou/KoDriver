package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveProximityUseCaseTest {

    @Test
    fun `invokeはリポジトリのproximityStreamを返す`() = runBlocking {
        val expected = fakeProximityData(isSideBySideLeft = true)
        val repo = FakeProximityRepository(stream = flowOf(expected))
        val useCase = ObserveProximityUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeProximityRepository(stream = flowOf())
        val useCase = ObserveProximityUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeProximityData(isSideBySideLeft = false, isSideBySideRight = false)
        val data2 = fakeProximityData(isSideBySideLeft = true, isSideBySideRight = false)
        val data3 = fakeProximityData(isSideBySideLeft = true, isSideBySideRight = true)
        val repo = FakeProximityRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveProximityUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}

internal class FakeProximityRepository(
    private val stream: Flow<ProximityData> = flowOf(),
) : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = stream
}

internal fun fakeProximityData(
    isSideBySideLeft: Boolean = false,
    isSideBySideRight: Boolean = false,
    lateralDistanceLeftMeters: Double = Double.MAX_VALUE,
    lateralDistanceRightMeters: Double = Double.MAX_VALUE,
) = ProximityData(
    isSideBySideLeft = isSideBySideLeft,
    isSideBySideRight = isSideBySideRight,
    lateralDistanceLeftMeters = lateralDistanceLeftMeters,
    lateralDistanceRightMeters = lateralDistanceRightMeters,
)
