package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * タイヤ内圧（単位: kPa）。他の生Doubleとの取り違えをコンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class LmuWindowsPressureKpa(
    val value: Double,
) : Comparable<LmuWindowsPressureKpa> {
    override fun compareTo(other: LmuWindowsPressureKpa): Int = value.compareTo(other.value)
}
