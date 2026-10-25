package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.readoutEnabled

class DetermineLmuWindowsPitTimingReadoutUseCase {
    /** バーチャルエネルギーを使うクラスでのみ警告する。 */
    fun determinePitTimingVirtualEnergy(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        virtualEnergy: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val trackingState =
            trackPitTimingValue(
                state = state.pitTimingVirtualEnergyTrackingState,
                currentLap = telemetry.timing.currentLap,
                bestLapTimeMs = telemetry.timing.bestLapTimeMs,
                currentValue = virtualEnergy.remainingRatio.value,
                session = virtualEnergy.session,
                observedAtMs = observedAtMs,
            )
        val stateAfterTracking =
            when {
                trackingState.isNewSession -> {
                    state.copy(
                        lastAnnouncedPitTimingVirtualEnergyLaps = -1,
                        lastPitTimingVirtualEnergyEvaluationLap = -1,
                        pitTimingVirtualEnergyTrackingState = trackingState,
                    )
                }

                trackingState.hasRefilled -> {
                    state.copy(
                        lastAnnouncedPitTimingVirtualEnergyLaps = -1,
                        pitTimingVirtualEnergyTrackingState = trackingState,
                    )
                }

                else -> {
                    state.copy(pitTimingVirtualEnergyTrackingState = trackingState)
                }
            }
        val evaluation =
            calculatePitTimingRemainingLaps(
                trackingState = trackingState,
                lastEvaluationLap = stateAfterTracking.lastPitTimingVirtualEnergyEvaluationLap,
                lastAnnouncedLaps = stateAfterTracking.lastAnnouncedPitTimingVirtualEnergyLaps,
                threshold = settings.pitTimingVirtualEnergyLapsThreshold,
                enabled =
                    settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.Root) &&
                        settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy) &&
                        settings.energySource == LmuWindowsEnergySource.VirtualEnergy,
            )
        val stateAfterEvaluation =
            stateAfterTracking.copy(lastPitTimingVirtualEnergyEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps =
            evaluation.remainingLaps ?: return LmuWindowsNarratorReadoutDecision(
                stateAfterEvaluation,
                emptyList(),
            )
        return LmuWindowsNarratorReadoutDecision(
            state = stateAfterEvaluation,
            events =
                listOf(
                    LmuWindowsPitTimingWarning(remainingLaps, source = PitTimingSource.VirtualEnergy),
                ),
        )
    }

    fun determinePitTimingTyreWear(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        tyreWear: LmuWindowsTyreWearData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        val worstRemainingRatio =
            tyreWear.wheels.values.minOrNull()
                ?: return LmuWindowsNarratorReadoutDecision(state, emptyList())
        val trackingState =
            trackPitTimingValue(
                state = state.pitTimingTyreWearTrackingState,
                currentLap = telemetry.timing.currentLap,
                bestLapTimeMs = telemetry.timing.bestLapTimeMs,
                currentValue = worstRemainingRatio.value,
                session = null,
                observedAtMs = observedAtMs,
            )
        val stateAfterTracking =
            when {
                trackingState.isNewSession -> {
                    state.copy(
                        lastAnnouncedPitTimingTyreWearLaps = -1,
                        lastPitTimingTyreWearEvaluationLap = -1,
                        pitTimingTyreWearTrackingState = trackingState,
                    )
                }

                trackingState.hasRefilled -> {
                    state.copy(
                        lastAnnouncedPitTimingTyreWearLaps = -1,
                        pitTimingTyreWearTrackingState = trackingState,
                    )
                }

                else -> {
                    state.copy(pitTimingTyreWearTrackingState = trackingState)
                }
            }
        val evaluation =
            calculatePitTimingRemainingLaps(
                trackingState = trackingState,
                lastEvaluationLap = stateAfterTracking.lastPitTimingTyreWearEvaluationLap,
                lastAnnouncedLaps = stateAfterTracking.lastAnnouncedPitTimingTyreWearLaps,
                threshold = settings.pitTimingTyreWearLapsThreshold,
                enabled =
                    settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.Root) &&
                        settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.TyreWear),
            )
        val stateAfterEvaluation =
            stateAfterTracking.copy(lastPitTimingTyreWearEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps =
            evaluation.remainingLaps ?: return LmuWindowsNarratorReadoutDecision(
                stateAfterEvaluation,
                emptyList(),
            )
        return LmuWindowsNarratorReadoutDecision(
            state = stateAfterEvaluation,
            events = listOf(LmuWindowsPitTimingWarning(remainingLaps, source = PitTimingSource.TyreWear)),
        )
    }

    fun determinePitTimingFuel(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        fuel: LmuWindowsRemainingFuelData,
        settings: LmuWindowsNarratorReadoutSettings,
        observedAtMs: Long,
    ): LmuWindowsNarratorReadoutDecision {
        if (!fuel.exactPercent.isFinite() || fuel.exactPercent !in 0.0..100.0) {
            return LmuWindowsNarratorReadoutDecision(state, emptyList())
        }
        val stateAfterTracking = trackFuelLaps(state, telemetry, fuel, observedAtMs)
        val trackingState = stateAfterTracking.fuelLapsTrackingState
        val evaluation =
            calculatePitTimingRemainingLaps(
                trackingState = trackingState,
                lastEvaluationLap = stateAfterTracking.lastPitTimingFuelEvaluationLap,
                lastAnnouncedLaps = stateAfterTracking.lastAnnouncedPitTimingFuelLaps,
                threshold = settings.pitTimingFuelLapsThreshold,
                enabled =
                    settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.Root) &&
                        settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.PitTiming.Fuel) &&
                        settings.energySource == LmuWindowsEnergySource.Fuel,
            )
        val stateAfterEvaluation =
            stateAfterTracking.copy(lastPitTimingFuelEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps =
            evaluation.remainingLaps ?: return LmuWindowsNarratorReadoutDecision(
                stateAfterEvaluation,
                emptyList(),
            )
        return LmuWindowsNarratorReadoutDecision(
            state = stateAfterEvaluation,
            events = listOf(LmuWindowsPitTimingWarning(remainingLaps, source = PitTimingSource.Fuel)),
        )
    }

    /** 読み上げイベントを生成せず、燃料の周回ごとの消費量を追跡する。 */
    fun trackFuelLaps(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        fuel: LmuWindowsRemainingFuelData,
        observedAtMs: Long,
    ): LmuWindowsNarratorState {
        if (!fuel.exactPercent.isFinite() || fuel.exactPercent < 0.0 || fuel.exactPercent > 100.0) return state
        val trackingState =
            trackPitTimingValue(
                state = state.fuelLapsTrackingState,
                currentLap = telemetry.timing.currentLap,
                bestLapTimeMs = telemetry.timing.bestLapTimeMs,
                currentValue = fuel.exactPercent / 100.0,
                session = null,
                observedAtMs = observedAtMs,
            )
        return state.copy(
            fuelLapsTrackingState = trackingState,
            lastAnnouncedPitTimingFuelLaps =
                if (trackingState.isNewSession ||
                    trackingState.hasRefilled
                ) {
                    -1
                } else {
                    state.lastAnnouncedPitTimingFuelLaps
                },
            lastPitTimingFuelEvaluationLap =
                if (trackingState.isNewSession) -1 else state.lastPitTimingFuelEvaluationLap,
        )
    }

    /**
     * ソース選択・周回ゲートを通過した警告だけを ViewModel から記録する。
     * 候補の生成時に記録すると、採用されなかったソースまで読み上げ済みになってしまう。
     */
    fun recordPitTimingAnnounced(
        state: LmuWindowsNarratorState,
        event: LmuWindowsPitTimingWarning,
    ): LmuWindowsNarratorState =
        when (event.source) {
            PitTimingSource.VirtualEnergy -> state.copy(lastAnnouncedPitTimingVirtualEnergyLaps = event.laps)
            PitTimingSource.TyreWear -> state.copy(lastAnnouncedPitTimingTyreWearLaps = event.laps)
            PitTimingSource.Fuel -> state.copy(lastAnnouncedPitTimingFuelLaps = event.laps)
        }
}

private const val PIT_TIMING_READOUT_BEFORE_BEST_LAP_MS = 30_000L

/** これ未満の残量増加はジッタとみなし、バーチャルエネルギー補充・タイヤ交換として扱わない（割合 0.0〜1.0 に対する値）。 */
private const val PIT_TIMING_REFILL_DETECTION_MIN_RATIO = 0.005

private data class PitTimingRemainingLapsEvaluation(
    val evaluatedLap: Int,
    val remainingLaps: Int?,
)

/** 補充を検出した、または値が基準より下がった場合だけ基準値を現在値へ更新し、閾値未満の増加では据え置く。 */
private fun nextRefillBaselineValue(
    baselineValue: Double,
    currentValue: Double,
    refilled: Double,
): Double = if (refilled > 0.0 || currentValue < baselineValue) currentValue else baselineValue

/**
 * ピットタイミング（バーチャルエネルギー・タイヤ摩耗）共通の追跡状態更新。
 * 直近に完走した（バーチャルエネルギー補充・タイヤ交換なしの）ラップの消費量を、次回以降の推定基準として使う。
 */
private fun trackPitTimingValue(
    state: LmuWindowsPitTimingTrackingState,
    currentLap: Int,
    bestLapTimeMs: Long,
    currentValue: Double,
    session: Int?,
    observedAtMs: Long,
): LmuWindowsPitTimingTrackingState =
    when {
        state.currentLap == -1 -> {
            LmuWindowsPitTimingTrackingState(
                session = session,
                currentLap = currentLap,
                currentLapStartedAtMs = observedAtMs,
                currentLapStartValue = currentValue,
                currentLapHasRefilled = false,
                currentValue = currentValue,
                refillBaselineValue = currentValue,
                lastValidLapConsumption = null,
                bestLapTimeMs = bestLapTimeMs,
                hasRefilled = false,
                isNewSession = false,
                observedAtMs = observedAtMs,
            )
        }

        (session != null && session != state.session) || currentLap < state.currentLap -> {
            LmuWindowsPitTimingTrackingState(
                session = session,
                currentLap = currentLap,
                currentLapStartedAtMs = observedAtMs,
                currentLapStartValue = currentValue,
                currentLapHasRefilled = false,
                currentValue = currentValue,
                refillBaselineValue = currentValue,
                lastValidLapConsumption = null,
                bestLapTimeMs = bestLapTimeMs,
                hasRefilled = false,
                isNewSession = true,
                observedAtMs = observedAtMs,
            )
        }

        else -> {
            // 共有メモリの値は微小な上振れ（ジッタ・torn read）を含みうるため、
            // 閾値未満の増加は基準値を据え置き、複数ポーリングに分かれた補充を累積で検出する。
            val delta = currentValue - state.refillBaselineValue
            val refilled = if (delta >= PIT_TIMING_REFILL_DETECTION_MIN_RATIO) delta else 0.0
            val refillBaselineValue = nextRefillBaselineValue(state.refillBaselineValue, currentValue, refilled)
            if (currentLap != state.currentLap) {
                // ラップが変わるタイミングで、直前のラップがバーチャルエネルギー補充・タイヤ交換なしで完走していれば
                // その消費量を今後の残り周回数推定の基準として採用する。
                // 境界直後の現在値ではなく前回観測値までを前周の消費とし、境界をまたぐ補充も除外する。
                val completedLapConsumption = state.currentLapStartValue - state.currentValue
                val lastValidLapConsumption =
                    if (
                        state.currentLapStartedAtBoundary &&
                        currentLap == state.currentLap + 1 &&
                        !state.currentLapHasRefilled &&
                        refilled == 0.0 &&
                        completedLapConsumption > 0.0
                    ) {
                        completedLapConsumption
                    } else {
                        state.lastValidLapConsumption
                    }
                state.copy(
                    session = session,
                    currentLap = currentLap,
                    currentLapStartedAtMs = observedAtMs,
                    currentLapStartValue = currentValue,
                    currentLapHasRefilled = refilled > 0.0,
                    currentLapStartedAtBoundary = currentLap == state.currentLap + 1,
                    currentValue = currentValue,
                    refillBaselineValue = refillBaselineValue,
                    lastValidLapConsumption = lastValidLapConsumption,
                    bestLapTimeMs = bestLapTimeMs,
                    hasRefilled = refilled > 0.0,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            } else {
                state.copy(
                    session = session,
                    currentLapHasRefilled = state.currentLapHasRefilled || refilled > 0.0,
                    currentValue = currentValue,
                    refillBaselineValue = refillBaselineValue,
                    bestLapTimeMs = bestLapTimeMs,
                    hasRefilled = refilled > 0.0,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }
        }
    }

// 時刻・有効な完走周の消費量が揃うまでは再試行し、推定後は設定無効などで読まなくても周回を評価済みにする。
// 読み上げ履歴の確定はここでは行わず、recordPitTimingAnnounced に委ねる。
private fun calculatePitTimingRemainingLaps(
    trackingState: LmuWindowsPitTimingTrackingState,
    lastEvaluationLap: Int,
    lastAnnouncedLaps: Int,
    threshold: Int,
    enabled: Boolean,
): PitTimingRemainingLapsEvaluation {
    if (trackingState.currentLap == lastEvaluationLap) {
        return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    }
    val bestLapTimeMs = trackingState.bestLapTimeMs
    if (bestLapTimeMs <= 0L) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    val readoutTimingMs = (bestLapTimeMs - PIT_TIMING_READOUT_BEFORE_BEST_LAP_MS).coerceAtLeast(0L)
    val currentLapElapsedMs = trackingState.observedAtMs - trackingState.currentLapStartedAtMs
    if (currentLapElapsedMs < readoutTimingMs) return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    val avgConsumption =
        trackingState.lastValidLapConsumption
            ?: return PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)
    if (avgConsumption <= 0.0) return PitTimingRemainingLapsEvaluation(trackingState.currentLap, null)
    val remainingLapsFloor = (trackingState.currentValue / avgConsumption).toInt()
    if (remainingLapsFloor < 0 || remainingLapsFloor > threshold) {
        return PitTimingRemainingLapsEvaluation(trackingState.currentLap, null)
    }
    if (remainingLapsFloor == lastAnnouncedLaps) return PitTimingRemainingLapsEvaluation(trackingState.currentLap, null)
    if (!enabled) return PitTimingRemainingLapsEvaluation(trackingState.currentLap, null)
    return PitTimingRemainingLapsEvaluation(trackingState.currentLap, remainingLapsFloor)
}
