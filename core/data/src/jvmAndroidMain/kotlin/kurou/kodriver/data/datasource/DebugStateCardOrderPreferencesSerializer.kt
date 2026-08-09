package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.DebugStateCardOrderPreferences

internal val DebugStateCardOrderPreferencesSerializer: Serializer<DebugStateCardOrderPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = DebugStateCardOrderPreferences(),
        kSerializer = DebugStateCardOrderPreferences.serializer(),
        typeName = "DebugStateCardOrderPreferences",
    )
