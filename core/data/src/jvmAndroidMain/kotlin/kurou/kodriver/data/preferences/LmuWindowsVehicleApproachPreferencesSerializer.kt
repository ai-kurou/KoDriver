package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleApproachPreferencesSerializer: Serializer<LmuWindowsVehicleApproachPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachPreferences(),
        kSerializer = LmuWindowsVehicleApproachPreferences.serializer(),
    )
