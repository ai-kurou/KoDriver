package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ReadoutHotkeyPreferencesSerializer: Serializer<ReadoutHotkeyPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutHotkeyPreferences(),
        kSerializer = ReadoutHotkeyPreferences.serializer(),
    )
