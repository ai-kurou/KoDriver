package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

public class SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    public suspend operator fun invoke(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    ): Unit = repository.saveLowThresholdPercent(vehicleClass, percent)
}
