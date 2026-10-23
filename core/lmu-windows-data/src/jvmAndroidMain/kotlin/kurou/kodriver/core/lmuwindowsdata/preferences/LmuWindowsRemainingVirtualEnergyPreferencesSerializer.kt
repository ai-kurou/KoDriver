package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsRemainingVirtualEnergyPreferencesSerializer:
    Serializer<LmuWindowsRemainingVirtualEnergyPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsRemainingVirtualEnergyPreferences(),
        kSerializer = LmuWindowsRemainingVirtualEnergyPreferences.serializer(),
    )
