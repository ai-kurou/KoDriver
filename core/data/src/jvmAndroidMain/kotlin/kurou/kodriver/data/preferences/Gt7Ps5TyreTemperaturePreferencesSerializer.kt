package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val Gt7Ps5TyreTemperaturePreferencesSerializer: Serializer<Gt7Ps5TyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = Gt7Ps5TyreTemperaturePreferences(),
        kSerializer = Gt7Ps5TyreTemperaturePreferences.serializer(),
    )
