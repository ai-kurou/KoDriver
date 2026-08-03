package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun fakeVehicleApproachData(
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

class ObserveLmuWindowsVehicleApproachUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsVehicleApproachRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのvehicleApproachStreamを返す`() =
        runTest {
            val expected = fakeVehicleApproachData(isSideBySideLeft = true)
            every { repo.vehicleApproachStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.vehicleApproachStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invokeは空のフローをそのまま返す`() =
        runTest {
            every { repo.vehicleApproachStream() } returns flowOf()
            val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.vehicleApproachStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = fakeVehicleApproachData(isSideBySideLeft = false, isSideBySideRight = false)
            val data2 = fakeVehicleApproachData(isSideBySideLeft = true, isSideBySideRight = false)
            val data3 = fakeVehicleApproachData(isSideBySideLeft = true, isSideBySideRight = true)
            every { repo.vehicleApproachStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsVehicleApproachUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.vehicleApproachStream() }
            confirmVerified(repo)
        }
}
