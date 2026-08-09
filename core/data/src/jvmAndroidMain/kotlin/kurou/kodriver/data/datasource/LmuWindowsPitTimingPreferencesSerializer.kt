package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences

internal val LmuWindowsPitTimingPreferencesSerializer: Serializer<LmuWindowsPitTimingPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsPitTimingPreferences(),
        kSerializer = LmuWindowsPitTimingPreferences.serializer(),
    )
