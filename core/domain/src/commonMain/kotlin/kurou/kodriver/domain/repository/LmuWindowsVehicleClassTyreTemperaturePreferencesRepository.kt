package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Celsius>>

    suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Celsius,
    )

    fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)
}
