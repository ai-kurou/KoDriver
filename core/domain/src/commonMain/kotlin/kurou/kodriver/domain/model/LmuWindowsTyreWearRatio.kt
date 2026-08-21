package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * LMU のタイヤ残溝割合（0.0-1.0。1.0=新品、0.0=摩耗限界）を表す値。
 * FuelPercent（0-100%）等の他レンジの値との取り違えをコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class LmuWindowsTyreWearRatio(
    val value: Double,
) : Comparable<LmuWindowsTyreWearRatio> {
    override fun compareTo(other: LmuWindowsTyreWearRatio): Int = value.compareTo(other.value)
}
