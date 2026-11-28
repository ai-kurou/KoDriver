package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val WakeWordHotkeyPreferencesSerializer: Serializer<WakeWordHotkeyPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = WakeWordHotkeyPreferences(),
        kSerializer = WakeWordHotkeyPreferences.serializer(),
    )
