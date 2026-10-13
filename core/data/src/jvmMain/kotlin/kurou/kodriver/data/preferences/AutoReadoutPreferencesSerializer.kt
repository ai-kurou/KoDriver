package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AutoReadoutPreferencesSerializer: Serializer<AutoReadoutPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AutoReadoutPreferences(),
        kSerializer = AutoReadoutPreferences.serializer(),
    )
