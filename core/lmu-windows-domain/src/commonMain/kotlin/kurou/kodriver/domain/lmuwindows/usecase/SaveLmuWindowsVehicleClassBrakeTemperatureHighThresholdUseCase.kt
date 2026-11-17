package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

public class SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ): Unit = repository.saveHighThresholdCelsius(vehicleClass, celsius)
}
