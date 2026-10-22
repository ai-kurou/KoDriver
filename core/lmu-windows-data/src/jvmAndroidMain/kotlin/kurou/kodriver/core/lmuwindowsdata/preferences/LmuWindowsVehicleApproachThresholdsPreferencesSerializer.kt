package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleApproachThresholdsPreferencesSerializer:
    Serializer<LmuWindowsVehicleApproachThresholdsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachThresholdsPreferences(),
        kSerializer = LmuWindowsVehicleApproachThresholdsPreferences.serializer(),
    )
