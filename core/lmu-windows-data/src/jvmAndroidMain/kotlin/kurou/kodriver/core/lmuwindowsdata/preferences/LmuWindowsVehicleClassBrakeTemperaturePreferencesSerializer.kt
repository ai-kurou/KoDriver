package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer:
    Serializer<LmuWindowsVehicleClassBrakeTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassBrakeTemperaturePreferences(),
        kSerializer = LmuWindowsVehicleClassBrakeTemperaturePreferences.serializer(),
    )
