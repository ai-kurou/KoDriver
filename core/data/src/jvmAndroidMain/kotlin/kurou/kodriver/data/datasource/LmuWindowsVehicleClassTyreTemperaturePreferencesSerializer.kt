package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsVehicleClassTyreTemperaturePreferences

internal val LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer:
    Serializer<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassTyreTemperaturePreferences(),
        kSerializer = LmuWindowsVehicleClassTyreTemperaturePreferences.serializer(),
        typeName = "LmuWindowsVehicleClassTyreTemperaturePreferences",
    )
