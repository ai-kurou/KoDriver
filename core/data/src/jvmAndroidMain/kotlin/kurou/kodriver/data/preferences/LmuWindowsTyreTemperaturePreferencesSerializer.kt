package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsTyreTemperaturePreferencesSerializer: Serializer<LmuWindowsTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreTemperaturePreferences(),
        kSerializer = LmuWindowsTyreTemperaturePreferences.serializer(),
    )
