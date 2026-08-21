package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * 並走車両との横方向距離（単位: meters）を表す値。他の生Doubleとの取り違えを
 * コンパイル時に防ぐために使う。並走車両がいない場合は [Double.MAX_VALUE] を表す。
 */
@Serializable
@JvmInline
value class LateralDistanceMeters(
    val value: Double,
) : Comparable<LateralDistanceMeters> {
    override fun compareTo(other: LateralDistanceMeters): Int = value.compareTo(other.value)
}
