package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.ConsoleAddressPreferences

internal val ConsoleAddressSerializer: Serializer<ConsoleAddressPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ConsoleAddressPreferences(),
        kSerializer = ConsoleAddressPreferences.serializer(),
    )
