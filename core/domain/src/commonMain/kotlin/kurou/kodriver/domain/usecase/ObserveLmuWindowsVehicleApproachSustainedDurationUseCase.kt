package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

class ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeSustainedApproachDurationSeconds()
}
