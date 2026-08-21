package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData

private const val PERCENT_MULTIPLIER = 100.0

internal data class FuelConsumptionResult(
    val consumptionPerLap: Double,
    val remainingLaps: Int,
    val preciseRemainingLaps: Double,
)

/**
 * LMUはリッター単位の燃料ではなくバーチャルエナジー残量割合（0.0〜1.0）を基準に走行するため、
 * 消費量はラップあたりの残量割合の減少をパーセント換算した値で表す。
 * 直近の給油・ピットインは考慮せず、レース開始からの平均消費量で近似する簡易計算。
 */
internal fun calculateLmuVirtualEnergyConsumption(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    telemetry: LmuWindowsTelemetryData?,
): FuelConsumptionResult? {
    val currentLap = telemetry?.timing?.currentLap ?: return null
    val remainingRatio = virtualEnergy?.remainingRatio ?: return null
    if (currentLap <= 0) return null
    val consumedRatio = 1.0 - remainingRatio
    if (consumedRatio <= 0.0) return null
    val avgConsumptionPerLap = consumedRatio / currentLap
    val preciseRemainingLaps = remainingRatio / avgConsumptionPerLap
    return FuelConsumptionResult(
        consumptionPerLap = avgConsumptionPerLap * PERCENT_MULTIPLIER,
        remainingLaps = preciseRemainingLaps.toInt(),
        preciseRemainingLaps = preciseRemainingLaps,
    )
}

/**
 * レース開始からの平均消費量（リッター/周）で近似する簡易計算。直近の給油は考慮しない。
 */
internal fun calculateGt7FuelConsumption(telemetry: Gt7Ps5TelemetryData?): FuelConsumptionResult? {
    if (telemetry == null || telemetry.lapCount <= 0) return null
    val consumedFuel = telemetry.gasCapacity - telemetry.gasLevel
    if (consumedFuel <= Gt7Ps5FuelUnit(0f)) return null
    val avgConsumptionPerLap = consumedFuel / telemetry.lapCount.toFloat()
    val preciseRemainingLaps = telemetry.gasLevel / avgConsumptionPerLap
    return FuelConsumptionResult(
        consumptionPerLap = avgConsumptionPerLap.value.toDouble(),
        remainingLaps = preciseRemainingLaps.toInt(),
        preciseRemainingLaps = preciseRemainingLaps.toDouble(),
    )
}

internal fun calculateGt7FuelRemainingPercent(telemetry: Gt7Ps5TelemetryData?): Double? {
    if (telemetry == null || telemetry.gasCapacity <= Gt7Ps5FuelUnit(0f)) return null
    return telemetry.gasLevel.value * PERCENT_MULTIPLIER / telemetry.gasCapacity.value
}

internal fun calculateLmuVirtualEnergyRemainingPercent(virtualEnergy: LmuWindowsVirtualEnergyData?): Double? =
    virtualEnergy?.remainingRatio?.times(PERCENT_MULTIPLIER)

/**
 * 4輪のうち最も摩耗が進んでいるタイヤの残溝割合を基準に、レース開始からの平均摩耗量で近似する簡易計算。
 * 直近のタイヤ交換は考慮しない。
 */
internal fun calculateLmuTyreWearRemainingLaps(telemetry: LmuWindowsTelemetryData?): Double? {
    val currentLap = telemetry?.timing?.currentLap ?: return null
    if (currentLap <= 0) return null
    val worstRemainingRatio =
        telemetry.tyres.wheels.values
            .minOfOrNull { it.wear }
            ?.value ?: return null
    val consumedRatio = 1.0 - worstRemainingRatio
    if (consumedRatio <= 0.0) return null
    val avgConsumptionPerLap = consumedRatio / currentLap
    return worstRemainingRatio / avgConsumptionPerLap
}
