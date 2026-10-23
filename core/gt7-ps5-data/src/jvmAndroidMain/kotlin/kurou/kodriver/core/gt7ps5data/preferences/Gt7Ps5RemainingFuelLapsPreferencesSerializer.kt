package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.Serializer

internal val Gt7Ps5RemainingFuelLapsPreferencesSerializer: Serializer<Gt7Ps5RemainingFuelLapsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5RemainingFuelLapsPreferences(),
        kSerializer = Gt7Ps5RemainingFuelLapsPreferences.serializer(),
    )
