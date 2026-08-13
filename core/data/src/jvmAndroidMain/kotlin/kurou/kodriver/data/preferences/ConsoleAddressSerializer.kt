package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ConsoleAddressSerializer: Serializer<ConsoleAddressPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ConsoleAddressPreferences(),
        kSerializer = ConsoleAddressPreferences.serializer(),
    )
