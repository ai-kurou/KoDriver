package kurou.kodriver.domain.util

import kotlin.math.round

/** 小数第2位を四捨五入して小数1桁の文字列にする。 */
fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
