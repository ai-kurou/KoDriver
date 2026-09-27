package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) = repository.saveHighThresholdCelsius(vehicleClass, celsius)
}
