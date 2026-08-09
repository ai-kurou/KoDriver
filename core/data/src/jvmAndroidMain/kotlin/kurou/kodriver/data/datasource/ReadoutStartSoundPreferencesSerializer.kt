package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.ReadoutStartSoundPreferences

internal val ReadoutStartSoundPreferencesSerializer: Serializer<ReadoutStartSoundPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutStartSoundPreferences(),
        kSerializer = ReadoutStartSoundPreferences.serializer(),
    )
