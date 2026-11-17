package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

public class SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    public suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData): Unit =
        repository.saveSelectedVehicleClass(vehicleClass)
}
