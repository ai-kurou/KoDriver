package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

public class ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeStartLeftReadoutText()
}
