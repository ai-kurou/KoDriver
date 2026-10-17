package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

private const val COOL_TEMPERATURE_CELSIUS = 70.0
private const val HOT_TEMPERATURE_CELSIUS = 110.0

internal fun temperatureHeatLevel(
    celsius: Double,
    coolThreshold: Double = COOL_TEMPERATURE_CELSIUS,
    warmThreshold: Double = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    hotThreshold: Double = HOT_TEMPERATURE_CELSIUS,
): HeatLevel =
    when {
        celsius < coolThreshold -> HeatLevel.COOL
        celsius < warmThreshold -> HeatLevel.OK
        celsius < hotThreshold -> HeatLevel.WARM
        else -> HeatLevel.HOT
    }
