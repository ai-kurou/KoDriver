package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository

public class ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeTyreDetachedReadoutText()
}
