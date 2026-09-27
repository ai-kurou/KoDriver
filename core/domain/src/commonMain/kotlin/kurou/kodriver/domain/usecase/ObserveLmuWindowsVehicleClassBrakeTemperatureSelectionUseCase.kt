package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.observeSelectedVehicleClass()
}
