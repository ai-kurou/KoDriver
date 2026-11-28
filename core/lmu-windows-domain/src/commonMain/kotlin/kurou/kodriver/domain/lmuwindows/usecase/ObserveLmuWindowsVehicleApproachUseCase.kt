package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_MINIMUM_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.model.LateralDistanceMeters
import kotlin.math.abs

/** 周辺車両の位置に、この端末に保存された距離閾値を適用して並走状態を購読する。 */
public class ObserveLmuWindowsVehicleApproachUseCase(
    private val repository: LmuWindowsVehicleApproachRepository,
    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleApproachData> =
        combine(
            repository.nearbyVehiclesStream(),
            thresholdsRepository.observeLongitudinalThresholdMeters(),
            thresholdsRepository.observeLateralThresholdMeters(),
            ::judgeSideBySide,
        )

    private fun judgeSideBySide(
        data: LmuWindowsNearbyVehiclesData,
        longitudinalThresholdMeters: Double,
        lateralThresholdMeters: Double,
    ): LmuWindowsVehicleApproachData {
        val lateralRange = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_MINIMUM_METERS_DEFAULT..lateralThresholdMeters
        val (left, right) =
            data.vehicles
                .filter {
                    abs(it.longitudinalMeters) < longitudinalThresholdMeters && abs(it.lateralMeters) in lateralRange
                }.partition { it.lateralMeters < 0 }
        return LmuWindowsVehicleApproachData(
            sideBySideLeftVehicleIds = left.mapTo(mutableSetOf()) { it.vehicleId },
            sideBySideRightVehicleIds = right.mapTo(mutableSetOf()) { it.vehicleId },
            lateralDistanceLeftMeters = nearestLateralDistance(left),
            lateralDistanceRightMeters = nearestLateralDistance(right),
        )
    }

    private fun nearestLateralDistance(vehicles: List<LmuWindowsNearbyVehicleData>): LateralDistanceMeters =
        LateralDistanceMeters(vehicles.minOfOrNull { abs(it.lateralMeters) } ?: Double.MAX_VALUE)
}
