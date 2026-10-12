package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

class ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeSustainedRightReadoutText()
}
