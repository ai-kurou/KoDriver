package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Int>> = repository.observeHighThresholdCelsius()
}
