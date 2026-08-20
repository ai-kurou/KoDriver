package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * テレメトリから取得した実測の摂氏温度。単位の取り違え（℃/℉、ケルビン等）をコンパイル時に防ぐために使う。
 * しきい値設定など整数で扱う値には [Celsius] を使う。
 */
@Serializable
@JvmInline
value class CelsiusReading(
    val value: Double,
) : Comparable<CelsiusReading> {
    override fun compareTo(other: CelsiusReading): Int = value.compareTo(other.value)
}
