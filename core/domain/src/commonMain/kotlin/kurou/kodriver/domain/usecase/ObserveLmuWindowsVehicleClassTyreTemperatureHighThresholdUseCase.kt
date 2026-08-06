package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Int>> = repository.observeHighThresholdCelsius()
}
