package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

class ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeStartRightReadoutText()
}
