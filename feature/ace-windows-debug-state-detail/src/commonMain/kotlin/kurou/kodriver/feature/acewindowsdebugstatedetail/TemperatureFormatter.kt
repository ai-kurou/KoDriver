package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import kotlin.math.round

internal fun wheelCarcassTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
): String = wheels[wheelIndex]?.let { formatCelsius(it) } ?: "-"

internal fun formatCelsius(value: CelsiusReading): String {
    val rounded = round(value.value * 10) / 10
    return rounded.toString()
}
