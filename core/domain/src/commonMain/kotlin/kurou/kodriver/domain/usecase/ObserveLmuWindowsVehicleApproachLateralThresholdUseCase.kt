package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ProximityThresholdsPreferencesRepository

class ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(
    private val repository: ProximityThresholdsPreferencesRepository,
) {
    operator fun invoke(): Flow<Double> = repository.observeLateralThresholdMeters()
}
