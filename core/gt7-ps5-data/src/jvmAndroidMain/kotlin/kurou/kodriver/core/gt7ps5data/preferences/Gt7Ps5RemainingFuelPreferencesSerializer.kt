package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.Serializer

internal val Gt7Ps5RemainingFuelPreferencesSerializer: Serializer<Gt7Ps5RemainingFuelPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5RemainingFuelPreferences(),
        kSerializer = Gt7Ps5RemainingFuelPreferences.serializer(),
    )
