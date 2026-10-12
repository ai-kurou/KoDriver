package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData) =
        repository.saveSelectedVehicleClass(vehicleClass)
}
