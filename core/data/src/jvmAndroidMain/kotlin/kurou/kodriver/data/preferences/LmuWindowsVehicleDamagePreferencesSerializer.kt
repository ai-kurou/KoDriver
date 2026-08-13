package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleDamagePreferencesSerializer: Serializer<LmuWindowsVehicleDamagePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleDamagePreferences(),
        kSerializer = LmuWindowsVehicleDamagePreferences.serializer(),
    )
