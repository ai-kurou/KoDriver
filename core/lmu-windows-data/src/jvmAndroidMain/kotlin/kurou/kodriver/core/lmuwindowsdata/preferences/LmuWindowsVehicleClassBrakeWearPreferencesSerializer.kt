package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleClassBrakeWearPreferencesSerializer:
    Serializer<LmuWindowsVehicleClassBrakeWearPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleClassBrakeWearPreferences(),
        kSerializer = LmuWindowsVehicleClassBrakeWearPreferences.serializer(),
    )
