package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsProximityUseCaseTest {

    @Test
    fun `invokeはリポジトリのproximityStreamを返す`() = runBlocking {
        val expected = fakeProximityData(isSideBySideLeft = true)
        val repo = FakeLmuWindowsProximityRepository(stream = flowOf(expected))
        val useCase = ObserveLmuWindowsProximityUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeLmuWindowsProximityRepository(stream = flowOf())
        val useCase = ObserveLmuWindowsProximityUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeProximityData(isSideBySideLeft = false, isSideBySideRight = false)
        val data2 = fakeProximityData(isSideBySideLeft = true, isSideBySideRight = false)
        val data3 = fakeProximityData(isSideBySideLeft = true, isSideBySideRight = true)
        val repo = FakeLmuWindowsProximityRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveLmuWindowsProximityUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}

internal class FakeLmuWindowsProximityRepository(
    private val stream: Flow<LmuWindowsProximityData> = flowOf(),
) : LmuWindowsProximityRepository {
    override fun proximityStream(): Flow<LmuWindowsProximityData> = stream
}

internal fun fakeProximityData(
    isSideBySideLeft: Boolean = false,
    isSideBySideRight: Boolean = false,
    lateralDistanceLeftMeters: Double = Double.MAX_VALUE,
    lateralDistanceRightMeters: Double = Double.MAX_VALUE,
) = LmuWindowsProximityData(
    sideBySideLeftVehicleIds = if (isSideBySideLeft) setOf(1) else emptySet(),
    sideBySideRightVehicleIds = if (isSideBySideRight) setOf(2) else emptySet(),
    lateralDistanceLeftMeters = lateralDistanceLeftMeters,
    lateralDistanceRightMeters = lateralDistanceRightMeters,
)
