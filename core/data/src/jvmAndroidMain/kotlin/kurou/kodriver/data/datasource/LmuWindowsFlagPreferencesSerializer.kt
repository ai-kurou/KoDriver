package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsFlagPreferences

internal val LmuWindowsFlagPreferencesSerializer: Serializer<LmuWindowsFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsFlagPreferences(),
        kSerializer = LmuWindowsFlagPreferences.serializer(),
    )
