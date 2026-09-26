package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsBrakeTemperaturePreferencesSerializer: Serializer<LmuWindowsBrakeTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsBrakeTemperaturePreferences(),
        kSerializer = LmuWindowsBrakeTemperaturePreferences.serializer(),
    )
