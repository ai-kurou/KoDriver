package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer:
    Serializer<LmuWindowsVehicleClassBrakeTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassBrakeTemperaturePreferences(),
        kSerializer = LmuWindowsVehicleClassBrakeTemperaturePreferences.serializer(),
    )
