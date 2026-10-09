package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * ブレーキの残り厚さ（単位: meters）。mm など他の長さ単位との取り違えを
 * コンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class BrakeThicknessMeters(
    val value: Float,
) : Comparable<BrakeThicknessMeters> {
    override fun compareTo(other: BrakeThicknessMeters): Int = value.compareTo(other.value)
}
