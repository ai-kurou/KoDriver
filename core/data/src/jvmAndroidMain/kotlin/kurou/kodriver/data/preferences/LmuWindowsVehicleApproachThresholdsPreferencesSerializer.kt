package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleApproachThresholdsPreferencesSerializer:
    Serializer<LmuWindowsVehicleApproachThresholdsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachThresholdsPreferences(),
        kSerializer = LmuWindowsVehicleApproachThresholdsPreferences.serializer(),
    )
