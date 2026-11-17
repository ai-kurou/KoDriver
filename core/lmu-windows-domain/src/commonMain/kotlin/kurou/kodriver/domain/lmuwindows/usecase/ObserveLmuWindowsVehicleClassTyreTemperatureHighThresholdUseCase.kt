package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<Map<LmuWindowsVehicleClassData, Celsius>> =
        repository.observeHighThresholdCelsius()
}
