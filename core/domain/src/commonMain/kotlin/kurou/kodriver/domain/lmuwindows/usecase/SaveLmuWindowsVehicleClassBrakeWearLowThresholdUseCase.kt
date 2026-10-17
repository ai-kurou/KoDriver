package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    ) = repository.saveLowThresholdPercent(vehicleClass, percent)
}
