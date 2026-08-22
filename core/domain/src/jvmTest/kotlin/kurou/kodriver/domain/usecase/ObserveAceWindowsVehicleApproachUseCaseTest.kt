@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsVehicleApproachUseCaseTest {
    @MockK
    private lateinit var repo: AceWindowsVehicleApproachRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの vehicleApproachStream を返す`() =
        runTest {
            val expected =
                AceWindowsVehicleApproachData(
                    nearbyVehicles = listOf(AceWindowsNearbyVehicleData(distanceMeters = 12.5)),
                )
            every { repo.vehicleApproachStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsVehicleApproachUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.vehicleApproachStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.vehicleApproachStream() } returns flowOf()
            val useCase = ObserveAceWindowsVehicleApproachUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.vehicleApproachStream() }
            confirmVerified(repo)
        }
}
