package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.WheelIndex
import kotlin.math.round

private const val LIFE_DECIMAL_SCALE = 1000

internal fun wheelLifeText(
    wheels: Map<WheelIndex, Double>,
    wheelIndex: WheelIndex,
): String = wheels[wheelIndex]?.let { formatLife(it) } ?: "-"

private fun formatLife(value: Double): String {
    val rounded = round(value * LIFE_DECIMAL_SCALE) / LIFE_DECIMAL_SCALE
    return rounded.toString()
}
