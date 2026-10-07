package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

class ObserveAceWindowsVehicleApproachReadoutTextUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
