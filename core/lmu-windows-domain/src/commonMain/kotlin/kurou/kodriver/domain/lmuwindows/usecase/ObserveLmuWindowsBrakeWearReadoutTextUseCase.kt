package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

class ObserveLmuWindowsBrakeWearReadoutTextUseCase(
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
