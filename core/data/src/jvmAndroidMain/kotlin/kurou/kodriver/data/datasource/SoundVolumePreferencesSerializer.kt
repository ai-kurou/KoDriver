package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.SoundVolumePreferences

internal val SoundVolumePreferencesSerializer: Serializer<SoundVolumePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SoundVolumePreferences(),
        kSerializer = SoundVolumePreferences.serializer(),
    )
