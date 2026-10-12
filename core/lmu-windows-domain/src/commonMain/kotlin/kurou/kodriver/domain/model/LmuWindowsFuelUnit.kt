package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * LMU の燃料量（リットル）を表す値。単位の取り違え（GT7のGt7Ps5FuelUnit・ACEのFuelPercent等との誤代入）を
 * コンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class LmuWindowsFuelUnit(
    val value: Double,
) : Comparable<LmuWindowsFuelUnit> {
    override fun compareTo(other: LmuWindowsFuelUnit): Int = value.compareTo(other.value)
}
