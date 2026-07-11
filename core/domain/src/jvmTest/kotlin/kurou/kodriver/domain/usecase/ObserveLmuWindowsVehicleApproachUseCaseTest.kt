package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleApproachUseCaseTest {

    @Test
    fun `invokeはリポジトリのvehicleApproachStreamを返す`() = runBlocking {
        val expected = fakeVehicleApproachData(isSideBySideLeft = true)
        val repo = FakeLmuWindowsVehicleApproachRepository(stream = flowOf(expected))
        val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleApproachRepository(stream = flowOf())
        val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeVehicleApproachData(isSideBySideLeft = false, isSideBySideRight = false)
        val data2 = fakeVehicleApproachData(isSideBySideLeft = true, isSideBySideRight = false)
        val data3 = fakeVehicleApproachData(isSideBySideLeft = true, isSideBySideRight = true)
        val repo = FakeLmuWindowsVehicleApproachRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}

internal class FakeLmuWindowsVehicleApproachRepository(
    private val stream: Flow<LmuWindowsVehicleApproachData> = flowOf(),
) : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = stream
}

internal fun fakeVehicleApproachData(
    isSideBySideLeft: Boolean = false,
    isSideBySideRight: Boolean = false,
    lateralDistanceLeftMeters: Double = Double.MAX_VALUE,
    lateralDistanceRightMeters: Double = Double.MAX_VALUE,
) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = if (isSideBySideLeft) setOf(1) else emptySet(),
    sideBySideRightVehicleIds = if (isSideBySideRight) setOf(2) else emptySet(),
    lateralDistanceLeftMeters = lateralDistanceLeftMeters,
    lateralDistanceRightMeters = lateralDistanceRightMeters,
)
