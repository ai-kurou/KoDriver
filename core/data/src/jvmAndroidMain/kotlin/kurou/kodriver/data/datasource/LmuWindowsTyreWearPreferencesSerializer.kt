package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences

internal val LmuWindowsTyreWearPreferencesSerializer: Serializer<LmuWindowsTyreWearPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreWearPreferences(),
        kSerializer = LmuWindowsTyreWearPreferences.serializer(),
    )
