package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.core.designsystem.temperatureHeatLevel as commonTemperatureHeatLevel

internal fun temperatureHeatLevel(celsius: Double): HeatLevel =
    commonTemperatureHeatLevel(
        celsius = celsius,
        warmThreshold = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    )
