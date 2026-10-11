package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

internal enum class HeatLevel {
    COOL,
    OK,
    WARM,
    HOT,
}

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
