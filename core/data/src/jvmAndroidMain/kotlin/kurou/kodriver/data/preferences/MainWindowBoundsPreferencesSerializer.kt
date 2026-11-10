package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val MainWindowBoundsPreferencesSerializer: Serializer<MainWindowBoundsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MainWindowBoundsPreferences(),
        kSerializer = MainWindowBoundsPreferences.serializer(),
    )
