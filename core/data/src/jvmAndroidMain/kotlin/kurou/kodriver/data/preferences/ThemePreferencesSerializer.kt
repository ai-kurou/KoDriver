package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ThemePreferencesSerializer: Serializer<ThemePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ThemePreferences(),
        kSerializer = ThemePreferences.serializer(),
    )
