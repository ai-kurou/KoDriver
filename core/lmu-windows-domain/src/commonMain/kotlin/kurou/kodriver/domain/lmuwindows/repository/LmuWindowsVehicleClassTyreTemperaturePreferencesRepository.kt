package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.Celsius

public interface LmuWindowsVehicleClassTyreTemperaturePreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Celsius>>

    public suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Celsius,
    )

    public fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    public suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)
}
