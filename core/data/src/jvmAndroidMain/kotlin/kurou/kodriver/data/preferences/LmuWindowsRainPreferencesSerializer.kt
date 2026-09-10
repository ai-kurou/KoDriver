package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsRainPreferencesSerializer: Serializer<LmuWindowsRainPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsRainPreferences(),
        kSerializer = LmuWindowsRainPreferences.serializer(),
    )
