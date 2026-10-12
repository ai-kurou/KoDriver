package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData) =
        repository.saveSelectedVehicleClass(vehicleClass)
}
