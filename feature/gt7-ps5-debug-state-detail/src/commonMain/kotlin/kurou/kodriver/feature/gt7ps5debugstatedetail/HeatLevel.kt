package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

internal enum class HeatLevel {
    COOL,
    OK,
    WARM,
    HOT,
}

private const val COOL_TEMPERATURE_CELSIUS = 70.0
private const val HOT_TEMPERATURE_CELSIUS = 110.0

// 読み上げ判定と同じ GT7 の既定閾値を使う。
internal fun tyreTemperatureHeatLevel(celsius: Double): HeatLevel =
    when {
        celsius < COOL_TEMPERATURE_CELSIUS -> HeatLevel.COOL
        celsius < GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble() -> HeatLevel.OK
        celsius < HOT_TEMPERATURE_CELSIUS -> HeatLevel.WARM
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
