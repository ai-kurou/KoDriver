package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences

internal val LmuWindowsVehicleApproachPreferencesSerializer: Serializer<LmuWindowsVehicleApproachPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachPreferences(),
        kSerializer = LmuWindowsVehicleApproachPreferences.serializer(),
    )
