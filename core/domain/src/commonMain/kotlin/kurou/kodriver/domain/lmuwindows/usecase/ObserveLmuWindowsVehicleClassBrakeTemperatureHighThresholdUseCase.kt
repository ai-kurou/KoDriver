package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Int>> = repository.observeHighThresholdCelsius()
}
