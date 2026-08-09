package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.data.model.LmuWindowsVehicleClassTyreTemperaturePreferences
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

internal class LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleClassTyreTemperaturePreferences>,
) : LmuWindowsVehicleClassTyreTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Int>> =
        dataStore.data.map { prefs ->
            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                prefs.highThresholdCelsiusByVehicleClass[keyOf(vehicleClass)]
                    ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass)
            }
        }

    override suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        dataStore.updateData {
            val updated = it.highThresholdCelsiusByVehicleClass + (keyOf(vehicleClass) to celsius)
            it.copy(highThresholdCelsiusByVehicleClass = updated)
        }
    }

    override fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData> =
        dataStore.data.map { prefs ->
            prefs.selectedVehicleClassKey
                .takeIf { it.isNotEmpty() }
                ?.let { LmuWindowsVehicleClassData.fromRawValue(it) }
                ?: LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT
        }

    override suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData) {
        dataStore.updateData { it.copy(selectedVehicleClassKey = keyOf(vehicleClass)) }
    }

    // Unknown は raw 値によらず1つのしきい値を共有する（未知クラス全体の安全網としての性質上、
    // raw文字列ごとに個別のしきい値を持たせる必要はないため）。
    private fun keyOf(vehicleClass: LmuWindowsVehicleClassData): String =
        if (vehicleClass is LmuWindowsVehicleClassData.Unknown) {
            LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
        } else {
            vehicleClass.name
        }
}
