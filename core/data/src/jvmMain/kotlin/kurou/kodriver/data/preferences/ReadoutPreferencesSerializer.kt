package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ReadoutPreferencesSerializer: Serializer<ReadoutPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutPreferences(),
        kSerializer = ReadoutPreferences.serializer(),
    )
