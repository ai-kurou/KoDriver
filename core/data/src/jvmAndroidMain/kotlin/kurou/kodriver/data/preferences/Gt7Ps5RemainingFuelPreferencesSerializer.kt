package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val Gt7Ps5RemainingFuelPreferencesSerializer: Serializer<Gt7Ps5RemainingFuelPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5RemainingFuelPreferences(),
        kSerializer = Gt7Ps5RemainingFuelPreferences.serializer(),
    )
