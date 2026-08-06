package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) = repository.saveHighThresholdCelsius(vehicleClass, celsius)
}
