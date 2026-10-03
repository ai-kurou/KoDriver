package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

class ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeStartLeftReadoutText()
}
