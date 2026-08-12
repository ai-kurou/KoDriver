package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.Gt7Ps5TyreTemperaturePreferences

internal val Gt7Ps5TyreTemperaturePreferencesSerializer: Serializer<Gt7Ps5TyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5TyreTemperaturePreferences(),
        kSerializer = Gt7Ps5TyreTemperaturePreferences.serializer(),
    )
