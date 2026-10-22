package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsTyreWearPreferencesSerializer: Serializer<LmuWindowsTyreWearPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsTyreWearPreferences(),
        kSerializer = LmuWindowsTyreWearPreferences.serializer(),
    )
