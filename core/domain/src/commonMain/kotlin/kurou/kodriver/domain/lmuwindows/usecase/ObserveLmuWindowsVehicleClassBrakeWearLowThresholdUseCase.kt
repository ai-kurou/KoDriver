package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Int>> = repository.observeLowThresholdPercent()
}
