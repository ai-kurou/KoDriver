package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class ObserveLmuWindowsBrakeWearReadoutTextUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
