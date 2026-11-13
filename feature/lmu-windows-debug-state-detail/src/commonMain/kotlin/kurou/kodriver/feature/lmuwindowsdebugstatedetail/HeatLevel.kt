package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_HEALTHY_REMAINING_PERCENT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT
import kurou.kodriver.core.designsystem.temperatureHeatLevel as commonTemperatureHeatLevel

internal fun temperatureHeatLevel(celsius: Double): HeatLevel =
    commonTemperatureHeatLevel(
        celsius = celsius,
        warmThreshold = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    )

internal fun brakeTemperatureHeatLevel(celsius: Double): HeatLevel =
    commonTemperatureHeatLevel(
        celsius = celsius,
        coolThreshold = LMU_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS,
        warmThreshold = LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT.toDouble(),
        hotThreshold = LMU_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS,
    )

// wear は摩耗量ではなく残溝割合。残量が少ないほど警告色にする。
internal fun wearHeatLevel(remainingPercent: Double): HeatLevel =
    when {
        remainingPercent < LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT -> HeatLevel.HOT
        remainingPercent < LMU_WINDOWS_TYRE_HEALTHY_REMAINING_PERCENT -> HeatLevel.WARM
        else -> HeatLevel.OK
    }
