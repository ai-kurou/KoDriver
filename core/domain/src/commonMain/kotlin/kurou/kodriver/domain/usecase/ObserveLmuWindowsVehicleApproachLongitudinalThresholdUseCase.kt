package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    operator fun invoke(): Flow<Double> = repository.observeLongitudinalThresholdMeters()
}
