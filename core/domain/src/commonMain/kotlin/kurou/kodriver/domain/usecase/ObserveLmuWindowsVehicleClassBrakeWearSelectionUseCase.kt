package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.observeSelectedVehicleClass()
}
