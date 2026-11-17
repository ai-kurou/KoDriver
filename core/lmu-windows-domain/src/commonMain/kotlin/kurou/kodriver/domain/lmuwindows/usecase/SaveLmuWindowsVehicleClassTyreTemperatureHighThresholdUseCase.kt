package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Celsius,
    ): Unit = repository.saveHighThresholdCelsius(vehicleClass, celsius)
}
