package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

public class SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData): Unit =
        repository.saveSelectedVehicleClass(vehicleClass)
}
