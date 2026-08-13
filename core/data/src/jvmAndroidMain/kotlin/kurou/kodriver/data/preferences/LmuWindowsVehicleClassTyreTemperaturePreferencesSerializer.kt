package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer:
    Serializer<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassTyreTemperaturePreferences(),
        kSerializer = LmuWindowsVehicleClassTyreTemperaturePreferences.serializer(),
    )
