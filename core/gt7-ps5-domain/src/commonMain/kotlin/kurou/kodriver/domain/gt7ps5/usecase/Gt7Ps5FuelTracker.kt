package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData

/** GT7 のテレメトリから燃料消費量の推定に使う追跡状態（給油・新セッション検出を含む）を更新する。 */
internal object Gt7Ps5FuelTracker {
    fun track(
        state: Gt7Ps5FuelTrackingState,
        telemetry: Gt7Ps5TelemetryData,
        observedAtMs: Long,
    ): Gt7Ps5FuelTrackingState =
        when {
            telemetry.lapCount < state.currentLap -> {
                Gt7Ps5FuelTrackingState(
                    raceStartFuel = telemetry.gasLevel,
                    raceStartLap = telemetry.lapCount,
                    currentLap = telemetry.lapCount,
                    currentLapStartedAtMs = observedAtMs,
                    currentGasLevel = telemetry.gasLevel,
                    refuelBaselineGasLevel = telemetry.gasLevel,
                    bestLapTimeMs = telemetry.bestLapTimeMs,
                    totalRefueled = Gt7Ps5FuelUnit(0f),
                    hasRefueled = false,
                    isNewSession = true,
                    observedAtMs = observedAtMs,
                )
            }

            state.raceStartFuel == null -> {
                Gt7Ps5FuelTrackingState(
                    raceStartFuel = telemetry.gasLevel,
                    raceStartLap = telemetry.lapCount,
                    currentLap = telemetry.lapCount,
                    currentLapStartedAtMs = observedAtMs,
                    currentGasLevel = telemetry.gasLevel,
                    refuelBaselineGasLevel = telemetry.gasLevel,
                    bestLapTimeMs = telemetry.bestLapTimeMs,
                    totalRefueled = Gt7Ps5FuelUnit(0f),
                    hasRefueled = false,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }

            else -> {
                val refueled =
                    detectRefueled(telemetry.gasLevel - state.refuelBaselineGasLevel, telemetry.gasCapacity)
                // 閾値未満の増加は基準値を据え置き、複数パケットに分かれた給油を累積で検出できるようにする
                val refuelBaselineGasLevel =
                    if (refueled > Gt7Ps5FuelUnit(0f) || telemetry.gasLevel < state.refuelBaselineGasLevel) {
                        telemetry.gasLevel
                    } else {
                        state.refuelBaselineGasLevel
                    }
                val currentLapStartedAtMs =
                    if (telemetry.lapCount != state.currentLap) {
                        observedAtMs
                    } else {
                        state.currentLapStartedAtMs
                    }
                state.copy(
                    currentLap = telemetry.lapCount,
                    currentLapStartedAtMs = currentLapStartedAtMs,
                    currentGasLevel = telemetry.gasLevel,
                    refuelBaselineGasLevel = refuelBaselineGasLevel,
                    bestLapTimeMs = telemetry.bestLapTimeMs,
                    totalRefueled = state.totalRefueled + refueled,
                    hasRefueled = refueled > Gt7Ps5FuelUnit(0f),
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }
        }

    /**
     * 給油判定の基準値からの増加量 [delta] のうち、給油として消費量の推定に加算すべき量。給油でなければ 0 を返す。
     *
     * UDP パケットの `gasLevel` は生の Float であり微小な上振れ（ジッタ・torn read）を含みうるため、
     * タンク容量に対して [REFUEL_DETECTION_MIN_RATIO] 未満の増加は給油とみなさない。閾値を持たないと
     * ジッタが `totalRefueled` に累積して消費量が過大に見積もられ、残り周回数が実際より少なく読み上げられる。
     * また給油とみなすたびに読み上げ履歴がリセットされるため、同じ周回数が繰り返し読み上げられる。
     *
     * タンク容量はICEで100前後・カートで5と車両により20倍の差があるため、絶対量ではなく容量に対する
     * 割合で判定する。容量が 0 以下（EV・未取得）の場合は燃料量に基づく推定自体が成立しないため、
     * 増加を給油とみなさない。
     */
    private fun detectRefueled(
        delta: Gt7Ps5FuelUnit,
        gasCapacity: Gt7Ps5FuelUnit,
    ): Gt7Ps5FuelUnit =
        if (gasCapacity > Gt7Ps5FuelUnit(0f) && delta.value >= gasCapacity.value * REFUEL_DETECTION_MIN_RATIO) {
            delta
        } else {
            Gt7Ps5FuelUnit(0f)
        }

    private const val REFUEL_DETECTION_MIN_RATIO = 0.005f
}
