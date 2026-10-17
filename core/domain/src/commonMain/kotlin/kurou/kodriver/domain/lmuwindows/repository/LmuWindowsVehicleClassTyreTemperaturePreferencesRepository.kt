package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.Celsius

interface LmuWindowsVehicleClassTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Celsius>>

    suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Celsius,
    )

    fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)
}
