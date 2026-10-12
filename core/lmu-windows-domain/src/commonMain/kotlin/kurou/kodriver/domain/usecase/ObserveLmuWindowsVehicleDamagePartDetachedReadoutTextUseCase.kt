package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

class ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observePartDetachedReadoutText()
}
