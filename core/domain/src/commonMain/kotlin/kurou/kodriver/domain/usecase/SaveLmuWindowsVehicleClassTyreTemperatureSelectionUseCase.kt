package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

class SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData) =
        repository.saveSelectedVehicleClass(vehicleClass)
}
