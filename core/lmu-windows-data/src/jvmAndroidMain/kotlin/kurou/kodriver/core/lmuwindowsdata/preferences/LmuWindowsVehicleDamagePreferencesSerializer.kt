package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleDamagePreferencesSerializer: Serializer<LmuWindowsVehicleDamagePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleDamagePreferences(),
        kSerializer = LmuWindowsVehicleDamagePreferences.serializer(),
    )
