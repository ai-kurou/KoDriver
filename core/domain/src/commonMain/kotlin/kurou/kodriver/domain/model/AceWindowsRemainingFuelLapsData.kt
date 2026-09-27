package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取った、残燃料で走行可能な周回数。
 */
@Serializable
data class AceWindowsRemainingFuelLapsData(
    /**
     * 残燃料で走行可能な周回数（`laps_possible_with_fuel`）。走行中の部分ラップを含む小数値で、
     * 直近の平均消費率（`fuel_liter_per_lap`）から ACE が算出する。消費実績がない間は 0 のことがある。
     */
    val remainingLaps: Float,
)
