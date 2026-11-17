package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData

public interface LmuWindowsVehicleClassBrakeWearPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeLowThresholdPercent(): Flow<Map<LmuWindowsVehicleClassData, Int>>

    public suspend fun saveLowThresholdPercent(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    )

    public fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData>

    public suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData)

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)
}
