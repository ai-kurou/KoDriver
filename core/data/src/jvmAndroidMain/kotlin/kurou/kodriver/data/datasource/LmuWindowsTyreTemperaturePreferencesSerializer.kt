package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences

internal val LmuWindowsTyreTemperaturePreferencesSerializer: Serializer<LmuWindowsTyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreTemperaturePreferences(),
        kSerializer = LmuWindowsTyreTemperaturePreferences.serializer(),
    )
