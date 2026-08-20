package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * リットル単位の燃料量を表す値。単位の取り違え（GT7のfuel unit・ACEの割合等との誤代入）をコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class Liters(
    val value: Double,
) : Comparable<Liters> {
    override fun compareTo(other: Liters): Int = value.compareTo(other.value)

    operator fun minus(other: Liters): Liters = Liters(value - other.value)
}
