package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ManualReadoutSetPreferencesSerializer: Serializer<ManualReadoutSetPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ManualReadoutSetPreferences(),
        kSerializer = ManualReadoutSetPreferences.serializer(),
    )
