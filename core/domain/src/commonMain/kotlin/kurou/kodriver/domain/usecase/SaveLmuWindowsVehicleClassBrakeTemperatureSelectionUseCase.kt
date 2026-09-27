package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(vehicleClass: LmuWindowsVehicleClassData) =
        repository.saveSelectedVehicleClass(vehicleClass)
}
