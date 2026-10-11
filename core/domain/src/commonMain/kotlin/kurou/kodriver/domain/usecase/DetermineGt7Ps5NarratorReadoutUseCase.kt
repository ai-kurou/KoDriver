package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled
import kotlin.math.roundToInt

/**
 * GT7 向け読み上げ判定の継続状態。
 *
 * 自己ベスト、燃料警告、残り燃料周回数の重複読み上げを避けるため、
 * 前回までの判定結果と燃料消費の追跡状態を保持する。
 * 自己ベストの状態は周回数の減少による新セッション検出時にリセットする。
 */
data class Gt7Ps5NarratorState(
    val personalBestMs: Int = Int.MAX_VALUE,
    val previousBestLapTimeMs: Int? = null,
    val previousLapCount: Int? = null,
    val lastAnnouncedRemainingLaps: Int = -1,
    val lastFuelEvaluationLap: Int = -1,
    val remainingFuelWarned: Boolean = false,
    val fuelTrackingState: Gt7Ps5FuelTrackingState = Gt7Ps5FuelTrackingState(),
    val tyreOverheating: Boolean = false,
)

/**
 * GT7 の燃料消費量推定に使う追跡状態。
 *
 * 閾値を超える燃料残量の増加は給油として扱い、レース開始時燃料・現在ラップ・経過時間から
 * 残り周回数警告のタイミングを推定する。
 */
data class Gt7Ps5FuelTrackingState(
    val raceStartFuel: Gt7Ps5FuelUnit? = null,
    val raceStartLap: Int? = null,
    val currentLap: Int = -1,
    val currentLapStartedAtMs: Long = 0L,
    val currentGasLevel: Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(0f),
    val refuelBaselineGasLevel: Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(0f),
    val bestLapTimeMs: Int = -1,
    val totalRefueled: Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(0f),
    val hasRefueled: Boolean = false,
    val isNewSession: Boolean = false,
    val observedAtMs: Long = 0L,
)

/** GT7 向け読み上げ判定で参照するユーザー設定。 */
data class Gt7Ps5NarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val remainingFuelLapsThreshold: Int,
    val remainingFuelThresholdPercentage: Int,
    val tyreTemperatureHighThresholdCelsius: Celsius,
)

/** GT7 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class Gt7Ps5NarratorReadoutDecision(
    val state: Gt7Ps5NarratorState,
    val events: List<SpeechEvent>,
)

/**
 * GT7 のテレメトリから、自己ベスト・燃料残量・残り燃料周回数・タイヤ温度の読み上げを決定する UseCase。
 */
class DetermineGt7Ps5NarratorReadoutUseCase {
    fun determineMyBestLap(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
    ): Gt7Ps5NarratorReadoutDecision {
        val current = telemetry.bestLapTimeMs
        val currentLap = telemetry.lapCount
        val sessionState =
            if (state.previousLapCount != null && currentLap < state.previousLapCount) {
                state.copy(personalBestMs = Int.MAX_VALUE, previousBestLapTimeMs = null)
            } else {
                state
            }
        val stateWithCurrentBestLap = sessionState.copy(previousBestLapTimeMs = current, previousLapCount = currentLap)
        val previous = sessionState.previousBestLapTimeMs
        if (previous == null) return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (current <= 0) return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (previous > 0 && current >= previous) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (current >= sessionState.personalBestMs) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (!settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.MyBestLap.Root) ||
            !settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled)
        ) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        val event = Gt7Ps5MyBestLap(current)
        return Gt7Ps5NarratorReadoutDecision(
            state = stateWithCurrentBestLap.copy(personalBestMs = current),
            events = listOf(event),
        )
    }

    fun determineRemainingFuelLaps(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
        observedAtMs: Long,
    ): Gt7Ps5NarratorReadoutDecision {
        val fuelTrackingState = trackFuel(state.fuelTrackingState, telemetry, observedAtMs)
        val stateAfterTracking =
            when {
                fuelTrackingState.isNewSession -> {
                    state.copy(
                        lastAnnouncedRemainingLaps = -1,
                        lastFuelEvaluationLap = -1,
                        fuelTrackingState = fuelTrackingState,
                    )
                }

                fuelTrackingState.hasRefueled -> {
                    state.copy(
                        lastAnnouncedRemainingLaps = -1,
                        fuelTrackingState = fuelTrackingState,
                    )
                }

                else -> {
                    state.copy(fuelTrackingState = fuelTrackingState)
                }
            }
        val evaluation = calculateRemainingFuelLaps(stateAfterTracking, settings)
        val stateAfterEvaluation = stateAfterTracking.copy(lastFuelEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps =
            evaluation.remainingLaps ?: return Gt7Ps5NarratorReadoutDecision(
                stateAfterEvaluation,
                emptyList(),
            )
        return Gt7Ps5NarratorReadoutDecision(
            state = stateAfterEvaluation.copy(lastAnnouncedRemainingLaps = remainingLaps),
            events = listOf(Gt7Ps5RemainingFuelLapsWarning(remainingLaps)),
        )
    }

    fun determineRemainingFuel(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
    ): Gt7Ps5NarratorReadoutDecision {
        val isLow = isLowRemainingFuel(telemetry, settings.remainingFuelThresholdPercentage)
        val shouldAnnounce =
            !state.remainingFuelWarned &&
                isLow &&
                settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuel.Root) &&
                settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled)
        return Gt7Ps5NarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events =
                if (shouldAnnounce) {
                    listOf(Gt7Ps5RemainingFuelWarning(remainingFuelPercent(telemetry)))
                } else {
                    emptyList()
                },
        )
    }

    fun determineTyreTemperature(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
    ): Gt7Ps5NarratorReadoutDecision {
        val wheels =
            listOf(
                telemetry.tyreTemperature.frontLeftCelsius,
                telemetry.tyreTemperature.frontRightCelsius,
                telemetry.tyreTemperature.rearLeftCelsius,
                telemetry.tyreTemperature.rearRightCelsius,
            )
        val hotThreshold = settings.tyreTemperatureHighThresholdCelsius.value.toFloat()
        val coolThreshold = hotThreshold - TYRE_OVERHEAT_HYSTERESIS_CELSIUS
        val anyHot = wheels.any { it.value >= hotThreshold }
        val allCool = wheels.all { it.value <= coolThreshold }
        val nextOverheating =
            when {
                anyHot -> true
                allCool -> false
                else -> state.tyreOverheating
            }
        val shouldAnnounce =
            !state.tyreOverheating && nextOverheating &&
                settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning)
        return Gt7Ps5NarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events =
                if (shouldAnnounce) {
                    listOf(Gt7Ps5TyreOverheat(wheels.maxOf { it.value }.roundToInt()))
                } else {
                    emptyList()
                },
        )
    }

    private fun trackFuel(
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

    private fun calculateRemainingFuelLaps(
        state: Gt7Ps5NarratorState,
        settings: Gt7Ps5NarratorReadoutSettings,
    ): RemainingFuelLapsEvaluation {
        val fuelState = state.fuelTrackingState
        if (fuelState.currentLap == state.lastFuelEvaluationLap) {
            return RemainingFuelLapsEvaluation(
                evaluatedLap = state.lastFuelEvaluationLap,
                remainingLaps = null,
            )
        }
        val bestLapTimeMs = fuelState.bestLapTimeMs
        if (bestLapTimeMs <= 0) return RemainingFuelLapsEvaluation(state.lastFuelEvaluationLap, null)
        val readoutTimingMs = (bestLapTimeMs - REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS).coerceAtLeast(0)
        val currentLapElapsedMs = fuelState.observedAtMs - fuelState.currentLapStartedAtMs
        if (currentLapElapsedMs < readoutTimingMs) return RemainingFuelLapsEvaluation(state.lastFuelEvaluationLap, null)
        val startFuel = fuelState.raceStartFuel ?: return RemainingFuelLapsEvaluation(state.lastFuelEvaluationLap, null)
        val startLap = fuelState.raceStartLap ?: return RemainingFuelLapsEvaluation(state.lastFuelEvaluationLap, null)
        val lapsCompleted = fuelState.currentLap - startLap
        if (lapsCompleted <= 0) return RemainingFuelLapsEvaluation(state.lastFuelEvaluationLap, null)
        val consumedFuel = startFuel + fuelState.totalRefueled - fuelState.currentGasLevel
        if (consumedFuel <= Gt7Ps5FuelUnit(0f)) return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        val avgConsumption = consumedFuel / (lapsCompleted + CURRENT_LAP_CONSUMPTION_WEIGHT)
        val remainingLapsFloor = (fuelState.currentGasLevel / avgConsumption).toInt()
        if (remainingLapsFloor < 0 || remainingLapsFloor > settings.remainingFuelLapsThreshold) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        if (remainingLapsFloor == state.lastAnnouncedRemainingLaps) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        if (!settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root) ||
            !settings.enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled)
        ) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        return RemainingFuelLapsEvaluation(fuelState.currentLap, remainingLapsFloor)
    }

    private fun remainingFuelPercent(telemetry: Gt7Ps5TelemetryData): Int =
        (telemetry.gasLevel.value / telemetry.gasCapacity.value * 100f).roundToInt().coerceIn(0, 100)

    private fun isLowRemainingFuel(
        telemetry: Gt7Ps5TelemetryData,
        thresholdPercentage: Int,
    ): Boolean =
        telemetry.gasLevel.value.isFinite() &&
            telemetry.gasCapacity.value.isFinite() &&
            telemetry.gasLevel > Gt7Ps5FuelUnit(0f) &&
            telemetry.gasCapacity > Gt7Ps5FuelUnit(0f) &&
            telemetry.gasLevel.value * 100f <= thresholdPercentage * telemetry.gasCapacity.value

    private companion object {
        const val REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS = 30_000
        const val CURRENT_LAP_CONSUMPTION_WEIGHT = 0.9f
        const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f

        /** これ未満の残量増加はジッタとみなし、給油として扱わない（タンク容量に対する割合 0.0〜1.0）。 */
        const val REFUEL_DETECTION_MIN_RATIO = 0.005f
    }
}

private data class RemainingFuelLapsEvaluation(
    val evaluatedLap: Int,
    val remainingLaps: Int?,
)
