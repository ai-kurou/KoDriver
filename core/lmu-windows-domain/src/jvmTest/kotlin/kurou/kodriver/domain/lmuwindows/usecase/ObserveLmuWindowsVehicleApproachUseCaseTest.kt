package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.model.LateralDistanceMeters
import kotlin.test.Test
import kotlin.test.assertEquals

private fun vehicle(
    id: Int,
    lateral: Double,
    longitudinal: Double = 0.0,
) = LmuWindowsNearbyVehicleData(vehicleId = id, lateralMeters = lateral, longitudinalMeters = longitudinal)

private fun approach(
    left: Set<Int> = emptySet(),
    right: Set<Int> = emptySet(),
    leftDistance: Double = Double.MAX_VALUE,
    rightDistance: Double = Double.MAX_VALUE,
) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = left,
    sideBySideRightVehicleIds = right,
    lateralDistanceLeftMeters = LateralDistanceMeters(leftDistance),
    lateralDistanceRightMeters = LateralDistanceMeters(rightDistance),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveLmuWindowsVehicleApproachUseCaseTest {
    private val repository: LmuWindowsVehicleApproachRepository = mockk()
    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository = mockk()

    private suspend fun judge(vararg vehicles: LmuWindowsNearbyVehicleData): LmuWindowsVehicleApproachData {
        every { repository.nearbyVehiclesStream() } returns
            flowOf(LmuWindowsNearbyVehiclesData(vehicles.toList()))
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns flowOf(4.5)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns flowOf(5.0)
        return ObserveLmuWindowsVehicleApproachUseCase(repository, thresholdsRepository)().first()
    }

    private fun verifyStreams() {
        verify(exactly = 1) { repository.nearbyVehiclesStream() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        confirmVerified(repository, thresholdsRepository)
    }

    @Test
    fun `左右の並走車を分けて側ごとの最短横距離を返す`() =
        runTest {
            val result = judge(vehicle(1, -5.0), vehicle(2, -3.0), vehicle(3, 2.0), vehicle(4, 4.0))

            assertEquals(approach(setOf(1, 2), setOf(3, 4), leftDistance = 3.0, rightDistance = 2.0), result)
            verifyStreams()
        }

    @Test
    fun `前後方向の距離が閾値以上の車両は並走判定しない`() =
        runTest {
            val result = judge(vehicle(1, -3.0, 4.5), vehicle(2, 3.0, -4.6), vehicle(3, 3.0, -4.4))

            assertEquals(approach(right = setOf(3), rightDistance = 3.0), result)
            verifyStreams()
        }

    @Test
    fun `横方向が最小値未満か閾値超過の車両は並走判定せず境界値は含める`() =
        runTest {
            val result = judge(vehicle(1, -0.9), vehicle(2, 5.1), vehicle(3, -1.0), vehicle(4, 5.0))

            assertEquals(approach(setOf(3), setOf(4), leftDistance = 1.0, rightDistance = 5.0), result)
            verifyStreams()
        }

    @Test
    fun `周辺車両がいない場合は並走なしになる`() =
        runTest {
            assertEquals(approach(), judge())
            verifyStreams()
        }

    @Test
    fun `端末の距離閾値が変わると同じ周辺車両を判定し直す`() =
        runTest {
            val longitudinal = MutableStateFlow(2.0)
            val lateral = MutableStateFlow(2.0)
            every { repository.nearbyVehiclesStream() } returns
                MutableStateFlow(LmuWindowsNearbyVehiclesData(listOf(vehicle(1, 3.0, 3.0))))
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinal
            every { thresholdsRepository.observeLateralThresholdMeters() } returns lateral
            val results = mutableListOf<LmuWindowsVehicleApproachData>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                ObserveLmuWindowsVehicleApproachUseCase(repository, thresholdsRepository)().toList(results)
            }

            lateral.value = 4.0
            longitudinal.value = 4.0

            assertEquals(approach(right = setOf(1), rightDistance = 3.0), results.last())
            assertEquals(approach(), results.first())
            verifyStreams()
        }
}
