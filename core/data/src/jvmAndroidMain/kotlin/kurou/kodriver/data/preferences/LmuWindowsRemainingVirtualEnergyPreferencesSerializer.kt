package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsRemainingVirtualEnergyPreferencesSerializer:
    Serializer<LmuWindowsRemainingVirtualEnergyPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsRemainingVirtualEnergyPreferences(),
        kSerializer = LmuWindowsRemainingVirtualEnergyPreferences.serializer(),
    )
