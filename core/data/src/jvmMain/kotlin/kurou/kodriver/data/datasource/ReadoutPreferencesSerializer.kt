package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.ReadoutPreferences

internal val ReadoutPreferencesSerializer: Serializer<ReadoutPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutPreferences(),
        kSerializer = ReadoutPreferences.serializer(),
    )
