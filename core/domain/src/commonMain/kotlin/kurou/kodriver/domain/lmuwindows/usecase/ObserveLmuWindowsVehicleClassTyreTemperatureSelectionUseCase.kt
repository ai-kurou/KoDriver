package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.observeSelectedVehicleClass()
}
