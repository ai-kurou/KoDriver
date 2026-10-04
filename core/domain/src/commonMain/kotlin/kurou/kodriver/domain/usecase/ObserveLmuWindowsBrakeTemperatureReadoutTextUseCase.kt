package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

class ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase(
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
