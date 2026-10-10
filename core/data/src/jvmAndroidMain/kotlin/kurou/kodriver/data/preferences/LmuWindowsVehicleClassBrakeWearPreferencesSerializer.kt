package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassBrakeWearPreferencesSerializer:
    Serializer<LmuWindowsVehicleClassBrakeWearPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassBrakeWearPreferences(),
        kSerializer = LmuWindowsVehicleClassBrakeWearPreferences.serializer(),
    )
