package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.WheelIndex
import kotlin.math.round

private const val KELVIN_OFFSET = 273.15

internal fun wheelTemperatureText(
    wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
    wheelIndex: WheelIndex,
): String = wheels[wheelIndex]?.let { formatCelsius(it.surfaceTemperatureK - KELVIN_OFFSET) } ?: "-"

internal fun wheelCarcassTemperatureText(
    wheels: Map<WheelIndex, Double>,
    wheelIndex: WheelIndex,
): String = wheels[wheelIndex]?.let { formatCelsius(it) } ?: "-"

private fun formatCelsius(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
