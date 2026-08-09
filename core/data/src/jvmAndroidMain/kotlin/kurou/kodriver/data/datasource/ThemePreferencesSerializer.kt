package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.ThemePreferences

internal val ThemePreferencesSerializer: Serializer<ThemePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ThemePreferences(),
        kSerializer = ThemePreferences.serializer(),
    )
