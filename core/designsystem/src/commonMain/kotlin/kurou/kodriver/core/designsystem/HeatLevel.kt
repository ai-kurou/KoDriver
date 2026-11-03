package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** シミュレーター固有の閾値判定から受け取る表示用の熱レベル。 */
enum class HeatLevel {
    COOL,
    OK,
    WARM,
    HOT,
}

/** 現在のテーマに対応する熱レベルの色を返す。 */
@Composable
fun heatColor(level: HeatLevel): Color = heatColor(level, KoDriverExtendedColors.current)

internal fun heatColor(
    level: HeatLevel,
    colors: ExtendedColorScheme,
): Color =
    when (level) {
        HeatLevel.COOL -> colors.heatCool
        HeatLevel.OK -> colors.heatOk
        HeatLevel.WARM -> colors.heatWarm
        HeatLevel.HOT -> colors.heatHot
    }

private const val COOL_TEMPERATURE_CELSIUS_DEFAULT = 70.0
private const val HOT_TEMPERATURE_CELSIUS_DEFAULT = 110.0

/**
 * 温度を熱レベルに分類する。[coolThreshold] 未満は COOL、[warmThreshold] 未満は OK、[hotThreshold] 未満は WARM、それ以上は HOT。
 * [warmThreshold] はシミュレーター固有の警告閾値のため、呼び出し側が渡す。
 */
fun temperatureHeatLevel(
    celsius: Double,
    warmThreshold: Double,
    coolThreshold: Double = COOL_TEMPERATURE_CELSIUS_DEFAULT,
    hotThreshold: Double = HOT_TEMPERATURE_CELSIUS_DEFAULT,
): HeatLevel =
    when {
        celsius < coolThreshold -> HeatLevel.COOL
        celsius < warmThreshold -> HeatLevel.OK
        celsius < hotThreshold -> HeatLevel.WARM
        else -> HeatLevel.HOT
    }
