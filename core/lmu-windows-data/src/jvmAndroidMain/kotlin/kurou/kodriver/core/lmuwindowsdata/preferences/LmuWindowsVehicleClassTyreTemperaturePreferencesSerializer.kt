package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer:
    Serializer<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassTyreTemperaturePreferences(),
        kSerializer = LmuWindowsVehicleClassTyreTemperaturePreferences.serializer(),
    )
