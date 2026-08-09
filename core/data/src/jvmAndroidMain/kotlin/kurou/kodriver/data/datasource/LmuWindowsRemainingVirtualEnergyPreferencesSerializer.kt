package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyPreferences

internal val LmuWindowsRemainingVirtualEnergyPreferencesSerializer:
    Serializer<LmuWindowsRemainingVirtualEnergyPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsRemainingVirtualEnergyPreferences(),
        kSerializer = LmuWindowsRemainingVirtualEnergyPreferences.serializer(),
    )
