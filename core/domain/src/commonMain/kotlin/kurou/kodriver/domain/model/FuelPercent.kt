package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * 燃料残量の割合(%)を表す値。単位の取り違え（LMUのLiters・GT7のFuel Unit等との誤代入）を
 * コンパイル時に防ぐために使う。0.0 より大きく 100.0 以下の値を想定する。
 */
@Serializable
@JvmInline
value class FuelPercent(
    val value: Double,
) : Comparable<FuelPercent> {
    override fun compareTo(other: FuelPercent): Int = value.compareTo(other.value)
}
