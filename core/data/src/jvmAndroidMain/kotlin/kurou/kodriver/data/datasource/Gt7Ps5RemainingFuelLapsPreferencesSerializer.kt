package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences

internal val Gt7Ps5RemainingFuelLapsPreferencesSerializer: Serializer<Gt7Ps5RemainingFuelLapsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5RemainingFuelLapsPreferences(),
        kSerializer = Gt7Ps5RemainingFuelLapsPreferences.serializer(),
        typeName = "Gt7Ps5RemainingFuelLapsPreferences",
    )
