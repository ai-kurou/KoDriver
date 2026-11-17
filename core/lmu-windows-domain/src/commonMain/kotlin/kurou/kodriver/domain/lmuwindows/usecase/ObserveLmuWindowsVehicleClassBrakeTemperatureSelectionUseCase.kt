package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

public class ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.observeSelectedVehicleClass()
}
