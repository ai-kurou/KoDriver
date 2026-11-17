package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

public class ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.observeSelectedVehicleClass()
}
