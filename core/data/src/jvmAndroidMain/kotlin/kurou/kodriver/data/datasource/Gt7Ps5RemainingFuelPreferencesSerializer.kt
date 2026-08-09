package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences

internal val Gt7Ps5RemainingFuelPreferencesSerializer: Serializer<Gt7Ps5RemainingFuelPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5RemainingFuelPreferences(),
        kSerializer = Gt7Ps5RemainingFuelPreferences.serializer(),
        typeName = "Gt7Ps5RemainingFuelPreferences",
    )
