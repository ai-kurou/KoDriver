package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassBrakeWearPreferencesRepository {
    fun observeLowThresholdPercent(): Flow<Map<LmuWindowsVehicleClassData, Int>>

    suspend fun saveLowThresholdPercent(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    )

    fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}
