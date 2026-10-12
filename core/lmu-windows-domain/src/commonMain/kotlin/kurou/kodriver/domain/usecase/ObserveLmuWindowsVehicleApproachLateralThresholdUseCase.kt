package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    operator fun invoke(): Flow<Double> = repository.observeLateralThresholdMeters()
}
