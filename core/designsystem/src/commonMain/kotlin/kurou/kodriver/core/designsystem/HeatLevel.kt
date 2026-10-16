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
