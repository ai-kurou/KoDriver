package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * タイヤ内圧（単位: kPa）。他の生Doubleとの取り違えをコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class PressureKpa(
    val value: Double,
) : Comparable<PressureKpa> {
    override fun compareTo(other: PressureKpa): Int = value.compareTo(other.value)
}
