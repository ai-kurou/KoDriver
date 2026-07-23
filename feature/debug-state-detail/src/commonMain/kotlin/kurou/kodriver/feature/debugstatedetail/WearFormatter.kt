package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.WheelIndex
import kotlin.math.round

private const val PERCENTAGE_SCALE = 100

internal fun wheelWearPercentText(wheels: Map<WheelIndex, LmuWindowsTyreWheelData>, wheelIndex: WheelIndex): String =
    wheels[wheelIndex]?.let { formatPercent(it.wear * PERCENTAGE_SCALE) } ?: "-"

private fun formatPercent(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
