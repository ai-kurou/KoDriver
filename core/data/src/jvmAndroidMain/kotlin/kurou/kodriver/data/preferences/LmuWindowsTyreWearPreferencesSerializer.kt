package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsTyreWearPreferencesSerializer: Serializer<LmuWindowsTyreWearPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreWearPreferences(),
        kSerializer = LmuWindowsTyreWearPreferences.serializer(),
    )
