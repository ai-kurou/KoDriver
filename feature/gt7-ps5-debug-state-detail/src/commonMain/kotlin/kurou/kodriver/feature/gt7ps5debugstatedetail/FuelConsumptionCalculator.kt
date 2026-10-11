package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData

private const val PERCENT_MULTIPLIER = 100.0

internal data class FuelConsumptionResult(
    val consumptionPerLap: Double,
    val remainingLaps: Int,
    val preciseRemainingLaps: Double,
)

/**
 * レース開始からの平均消費量（リッター/周）で近似する簡易計算。直近の給油は考慮しない。
 */
internal fun calculateGt7FuelConsumption(telemetry: Gt7Ps5TelemetryData?): FuelConsumptionResult? {
    if (telemetry == null || telemetry.lapCount <= 0 || !telemetry.hasFiniteFuel()) return null
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
    if (telemetry == null || !telemetry.hasFiniteFuel() || telemetry.gasCapacity <= Gt7Ps5FuelUnit(0f)) return null
    return telemetry.gasLevel.value * PERCENT_MULTIPLIER / telemetry.gasCapacity.value
}

private fun Gt7Ps5TelemetryData.hasFiniteFuel(): Boolean = gasLevel.value.isFinite() && gasCapacity.value.isFinite()
