package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Int>> = repository.observeHighThresholdCelsius()
}
