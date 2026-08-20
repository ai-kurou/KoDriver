package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * 摂氏温度を表す値。単位の取り違え（℃/℉、他の閾値との誤代入等）をコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class Celsius(
    val value: Int,
) : Comparable<Celsius> {
    override fun compareTo(other: Celsius): Int = value.compareTo(other.value)

    operator fun minus(other: Celsius): Celsius = Celsius(value - other.value)
}
