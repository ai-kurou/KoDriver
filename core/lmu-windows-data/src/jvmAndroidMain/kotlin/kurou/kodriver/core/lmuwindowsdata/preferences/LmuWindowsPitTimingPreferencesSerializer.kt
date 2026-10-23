package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsPitTimingPreferencesSerializer: Serializer<LmuWindowsPitTimingPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsPitTimingPreferences(),
        kSerializer = LmuWindowsPitTimingPreferences.serializer(),
    )
