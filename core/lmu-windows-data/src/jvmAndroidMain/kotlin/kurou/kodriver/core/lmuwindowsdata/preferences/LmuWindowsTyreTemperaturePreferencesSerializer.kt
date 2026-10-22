package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsTyreTemperaturePreferencesSerializer: Serializer<LmuWindowsTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreTemperaturePreferences(),
        kSerializer = LmuWindowsTyreTemperaturePreferences.serializer(),
    )
