package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

internal val Gt7Ps5TyreTemperaturePreferencesSerializer: Serializer<Gt7Ps5TyreTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue =
            Gt7Ps5TyreTemperaturePreferences(
                highThresholdCelsius = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
            ),
        kSerializer = Gt7Ps5TyreTemperaturePreferences.serializer(),
    )
