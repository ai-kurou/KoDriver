package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData

private const val PERCENT_MULTIPLIER = 100.0

internal data class FuelConsumptionResult(
    val consumptionPerLap: Double,
    val remainingLaps: Int,
    val preciseRemainingLaps: Double,
)

/**
 * LMUはリッター単位の燃料ではなくバーチャルエネルギー残量割合（0.0〜1.0）を基準に走行するため、
 * 消費量はラップあたりの残量割合の減少をパーセント換算した値で表す。
 * 観測開始・補給後の基準点からの平均消費量で近似する簡易計算。
 */
internal fun calculateLmuVirtualEnergyConsumption(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    telemetry: LmuWindowsTelemetryData?,
    baseline: LmuWindowsVirtualEnergyBaseline?,
): FuelConsumptionResult? {
    val currentLap = telemetry?.timing?.currentLap ?: return null
    val remainingRatio = virtualEnergy?.remainingRatio?.value ?: return null
    if (baseline == null || !remainingRatio.isFinite()) return null
    val elapsedLaps = currentLap - baseline.lap
    val consumedRatio = baseline.ratio - remainingRatio
    if (elapsedLaps <= 0 || consumedRatio <= 0.0) return null
    val avgConsumptionPerLap = consumedRatio / elapsedLaps
    val preciseRemainingLaps = remainingRatio / avgConsumptionPerLap
    return FuelConsumptionResult(
        consumptionPerLap = avgConsumptionPerLap * PERCENT_MULTIPLIER,
        remainingLaps = preciseRemainingLaps.toInt(),
        preciseRemainingLaps = preciseRemainingLaps,
    )
}

internal fun calculateLmuVirtualEnergyRemainingPercent(virtualEnergy: LmuWindowsVirtualEnergyData?): Double? =
    virtualEnergy?.remainingRatio?.value?.times(PERCENT_MULTIPLIER)

/**
 * 4輪のうち最も摩耗が進んでいるタイヤの残溝割合を基準に、観測開始・タイヤ交換後の基準点からの平均摩耗量で近似する簡易計算。
 */
internal fun calculateLmuTyreWearRemainingLaps(
    telemetry: LmuWindowsTelemetryData?,
    baseline: LmuWindowsTyreWearBaseline?,
): Double? {
    val currentLap = telemetry?.timing?.currentLap ?: return null
    val worstRemainingRatio =
        telemetry.tyres.wheels.values
            .minOfOrNull { it.wear }
            ?.value ?: return null
    if (baseline == null || !worstRemainingRatio.isFinite()) return null
    val elapsedLaps = currentLap - baseline.lap
    val consumedRatio = baseline.ratio - worstRemainingRatio
    if (elapsedLaps <= 0 || consumedRatio <= 0.0) return null
    val avgConsumptionPerLap = consumedRatio / elapsedLaps
    return worstRemainingRatio / avgConsumptionPerLap
}

/**
 * 観測開始（または給油後）の燃料量との差分と経過周回数から、平均消費量で残り周回数を近似する。
 * 消費量が未確定（経過周回数・消費量が0以下）の場合は null を返す。
 */
internal fun calculateLmuFuelRemainingLaps(
    telemetry: LmuWindowsTelemetryData?,
    baseline: LmuWindowsFuelBaseline?,
): Double? {
    val currentLap = telemetry?.timing?.currentLap ?: return null
    val current = telemetry.fuel.currentLiters.value
    if (baseline == null || !current.isFinite() || current < 0.0) return null
    val elapsedLaps = currentLap - baseline.lap
    val consumedLiters = baseline.liters - current
    if (elapsedLaps <= 0 || consumedLiters <= 0.0) return null
    return current / (consumedLiters / elapsedLaps)
}
