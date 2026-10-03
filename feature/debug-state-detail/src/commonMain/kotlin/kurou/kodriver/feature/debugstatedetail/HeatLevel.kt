package kurou.kodriver.feature.debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT
import kurou.kodriver.domain.model.Simulator

internal enum class HeatLevel {
    COOL,
    OK,
    WARM,
    HOT,
}

private const val COOL_TEMPERATURE_CELSIUS = 70.0
private const val HOT_TEMPERATURE_CELSIUS = 110.0
private const val HEALTHY_REMAINING_PERCENT = 75.0
private const val COOL_BRAKE_TEMPERATURE_CELSIUS = 400.0
private const val HOT_BRAKE_TEMPERATURE_CELSIUS = 1000.0

internal fun temperatureHeatLevel(
    celsius: Double,
    coolThreshold: Double = COOL_TEMPERATURE_CELSIUS,
    warmThreshold: Double = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    hotThreshold: Double = HOT_TEMPERATURE_CELSIUS,
): HeatLevel =
    when {
        celsius < coolThreshold -> HeatLevel.COOL
        celsius < warmThreshold -> HeatLevel.OK
        celsius < hotThreshold -> HeatLevel.WARM
        else -> HeatLevel.HOT
    }

// タイヤ内部温度の警告温度は読み上げ判定と同じく、シミュレーターごとの既定値を使う。
internal fun tyreTemperatureHeatLevel(
    celsius: Double,
    simulator: Simulator,
): HeatLevel =
    when (simulator) {
        is Simulator.AceWindows -> {
            temperatureHeatLevel(
                celsius = celsius,
                warmThreshold = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
            )
        }

        is Simulator.LmuWindows, is Simulator.Gt7Ps5 -> {
            temperatureHeatLevel(celsius)
        }
    }

internal fun brakeTemperatureHeatLevel(celsius: Double): HeatLevel =
    temperatureHeatLevel(
        celsius = celsius,
        coolThreshold = COOL_BRAKE_TEMPERATURE_CELSIUS,
        warmThreshold = LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT.toDouble(),
        hotThreshold = HOT_BRAKE_TEMPERATURE_CELSIUS,
    )

// wear は摩耗量ではなく残溝割合。残量が少ないほど警告色にする。
internal fun wearHeatLevel(remainingPercent: Double): HeatLevel =
    when {
        remainingPercent < LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT -> HeatLevel.HOT
        remainingPercent < HEALTHY_REMAINING_PERCENT -> HeatLevel.WARM
        else -> HeatLevel.OK
    }

@Composable
internal fun heatColor(level: HeatLevel): Color {
    val colors = KoDriverExtendedColors.current
    return when (level) {
        HeatLevel.COOL -> colors.heatCool
        HeatLevel.OK -> colors.heatOk
        HeatLevel.WARM -> colors.heatWarm
        HeatLevel.HOT -> colors.heatHot
    }
}
