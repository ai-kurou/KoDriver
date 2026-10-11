package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlin.math.round

internal fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
