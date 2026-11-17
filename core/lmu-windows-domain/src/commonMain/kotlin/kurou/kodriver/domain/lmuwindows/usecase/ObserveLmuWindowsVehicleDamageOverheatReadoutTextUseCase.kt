package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository

public class ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
