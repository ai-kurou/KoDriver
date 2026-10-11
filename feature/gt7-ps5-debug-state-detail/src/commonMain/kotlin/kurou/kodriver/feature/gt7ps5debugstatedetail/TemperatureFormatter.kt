package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kotlin.math.round

internal fun formatCelsius(value: CelsiusReading): String {
    val rounded = round(value.value * 10) / 10
    return rounded.toString()
}
