package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

public class ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeStartRightReadoutText()
}
