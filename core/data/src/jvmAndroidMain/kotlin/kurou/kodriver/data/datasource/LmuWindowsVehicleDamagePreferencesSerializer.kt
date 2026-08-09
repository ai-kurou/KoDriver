package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsVehicleDamagePreferences

internal val LmuWindowsVehicleDamagePreferencesSerializer: Serializer<LmuWindowsVehicleDamagePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleDamagePreferences(),
        kSerializer = LmuWindowsVehicleDamagePreferences.serializer(),
        typeName = "LmuWindowsVehicleDamagePreferences",
    )
