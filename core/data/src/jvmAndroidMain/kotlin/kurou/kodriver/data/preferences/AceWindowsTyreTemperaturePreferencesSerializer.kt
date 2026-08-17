package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsTyreTemperaturePreferencesSerializer: Serializer<AceWindowsTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsTyreTemperaturePreferences(),
        kSerializer = AceWindowsTyreTemperaturePreferences.serializer(),
    )
