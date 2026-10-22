package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository

class ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observePartDetachedReadoutText()
}
