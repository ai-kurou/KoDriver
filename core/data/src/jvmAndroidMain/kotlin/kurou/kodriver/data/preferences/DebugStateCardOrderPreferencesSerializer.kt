package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val DebugStateCardOrderPreferencesSerializer: Serializer<DebugStateCardOrderPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = DebugStateCardOrderPreferences(),
        kSerializer = DebugStateCardOrderPreferences.serializer(),
    )
