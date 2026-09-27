package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

internal class LmuWindowsVehicleClassBrakeTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleClassBrakeTemperaturePreferences>,
) : LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Int>> =
        dataStore.observeProperty { prefs ->
            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                prefs.highThresholdCelsiusByVehicleClass[keyOf(vehicleClass)]
                    ?: lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(vehicleClass)
            }
        }

    override suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        dataStore.saveProperty(celsius) { prefs, value ->
            val updated = prefs.highThresholdCelsiusByVehicleClass + (keyOf(vehicleClass) to value)
            prefs.copy(highThresholdCelsiusByVehicleClass = updated)
        }
    }

    override fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData> =
        dataStore.observeProperty { prefs ->
            prefs.selectedVehicleClassKey
                .takeIf { it.isNotEmpty() }
                ?.let { LmuWindowsVehicleClassData.fromRawValue(it) }
                ?: LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
        }

    override suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData) {
        dataStore.saveProperty(keyOf(vehicleClass)) { prefs, value -> prefs.copy(selectedVehicleClassKey = value) }
    }

    // Unknown は raw 値によらず1つの閾値を共有する（未知クラス全体の安全網としての性質上、
    // raw文字列ごとに個別の閾値を持たせる必要はないため）。
    private fun keyOf(vehicleClass: LmuWindowsVehicleClassData): String =
        if (vehicleClass is LmuWindowsVehicleClassData.Unknown) {
            LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
        } else {
            vehicleClass.name
        }
}
