package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ManualReadoutPlayButtonLayoutPreferencesSerializer: Serializer<ManualReadoutPlayButtonLayoutPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ManualReadoutPlayButtonLayoutPreferences(),
        kSerializer = ManualReadoutPlayButtonLayoutPreferences.serializer(),
    )
