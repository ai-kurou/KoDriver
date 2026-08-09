package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences

internal val LmuWindowsVehicleApproachThresholdsPreferencesSerializer:
    Serializer<LmuWindowsVehicleApproachThresholdsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachThresholdsPreferences(),
        kSerializer = LmuWindowsVehicleApproachThresholdsPreferences.serializer(),
        typeName = "LmuWindowsVehicleApproachThresholdsPreferences",
    )
