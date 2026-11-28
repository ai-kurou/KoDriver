package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsNearbyVehiclesUseCaseTest {
    private val repository: LmuWindowsVehicleApproachRepository = mockk()

    @Test
    fun `閾値を適用せずリポジトリの周辺車両を順番通りに流す`() =
        runTest {
            val data =
                listOf(
                    LmuWindowsNearbyVehiclesData(emptyList()),
                    LmuWindowsNearbyVehiclesData(listOf(LmuWindowsNearbyVehicleData(1, -8.0, 10.0))),
                )
            every { repository.nearbyVehiclesStream() } returns flowOf(data[0], data[1])

            val results = ObserveLmuWindowsNearbyVehiclesUseCase(repository)().toList()

            assertEquals(data, results)
            verify(exactly = 1) { repository.nearbyVehiclesStream() }
            confirmVerified(repository)
        }
}
