package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Int>>

    suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    )

    fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)
}
