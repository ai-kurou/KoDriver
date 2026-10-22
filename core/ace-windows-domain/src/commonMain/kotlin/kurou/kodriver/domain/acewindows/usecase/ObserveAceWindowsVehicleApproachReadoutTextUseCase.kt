package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository

class ObserveAceWindowsVehicleApproachReadoutTextUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
