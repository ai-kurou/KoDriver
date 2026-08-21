package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * LMU のバーチャルエナジー残量割合（0.0-1.0。1.0=満タン、0.0=空）を表す値。
 * FuelPercent（0-100%）等の他レンジの値との取り違えをコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class LmuWindowsVirtualEnergyRatio(
    val value: Double,
) : Comparable<LmuWindowsVirtualEnergyRatio> {
    override fun compareTo(other: LmuWindowsVirtualEnergyRatio): Int = value.compareTo(other.value)
}
