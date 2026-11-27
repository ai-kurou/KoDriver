package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.core.designsystem.temperatureHeatLevel as commonTemperatureHeatLevel

internal fun temperatureHeatLevel(celsius: Double): HeatLevel =
    commonTemperatureHeatLevel(
        celsius = celsius,
        warmThreshold = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    )

internal fun brakeTemperatureHeatLevel(celsius: Double): HeatLevel =
    commonTemperatureHeatLevel(
        celsius = celsius,
        coolThreshold = ACE_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS,
        warmThreshold = ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
        hotThreshold = ACE_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS,
    )
