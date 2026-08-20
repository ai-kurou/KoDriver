package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * GT7 の燃料量（リットル）を表す値。単位の取り違え（LMUのLiters・ACEのFuelPercent等との誤代入）を
 * コンパイル時に防ぐために使う。
 */
@Serializable
@JvmInline
value class Gt7Ps5FuelUnit(
    val value: Float,
) : Comparable<Gt7Ps5FuelUnit> {
    override fun compareTo(other: Gt7Ps5FuelUnit): Int = value.compareTo(other.value)

    operator fun plus(other: Gt7Ps5FuelUnit): Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(value + other.value)

    operator fun minus(other: Gt7Ps5FuelUnit): Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(value - other.value)

    /** ラップ数などのスカラーで割り、ラップあたりの消費量を求める。 */
    operator fun div(laps: Float): Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(value / laps)

    /** ラップあたりの消費量で割り、残り周回数（無次元）を求める。 */
    operator fun div(consumptionPerLap: Gt7Ps5FuelUnit): Float = value / consumptionPerLap.value

    fun coerceAtLeast(minimum: Gt7Ps5FuelUnit): Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(value.coerceAtLeast(minimum.value))
}
