package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/**
 * GT7 向け読み上げ判定の継続状態。
 *
 * 自己ベスト、燃料警告、残り燃料周回数の重複読み上げを避けるため、
 * 前回までの判定結果と燃料消費の追跡状態を保持する。
 */
data class Gt7Ps5NarratorState(
    val personalBestMs: Int = Int.MAX_VALUE,
    val previousBestLapTimeMs: Int? = null,
    val lastAnnouncedRemainingLaps: Int = -1,
    val lastFuelEvaluationLap: Int = -1,
    val remainingFuelWarned: Boolean = false,
    val fuelTrackingState: Gt7Ps5FuelTrackingState = Gt7Ps5FuelTrackingState(),
    val tyreOverheating: Boolean = false,
)

/**
 * GT7 の燃料消費量推定に使う追跡状態。
 *
 * 燃料残量の増加は給油として扱い、レース開始時燃料・現在ラップ・経過時間から
 * 残り周回数警告のタイミングを推定する。
 */
data class Gt7Ps5FuelTrackingState(
    val raceStartFuel: Gt7Ps5FuelUnit? = null,
    val raceStartLap: Int? = null,
    val currentLap: Int = -1,
    val currentLapStartedAtMs: Long = 0L,
    val currentGasLevel: Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(0f),
    val bestLapTimeMs: Int = -1,
    val totalRefueled: Gt7Ps5FuelUnit = Gt7Ps5FuelUnit(0f),
    val hasRefueled: Boolean = false,
    val isNewSession: Boolean = false,
    val observedAtMs: Long = 0L,
)

/** GT7 向け読み上げ判定で参照するユーザー設定。 */
data class Gt7Ps5NarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val myBestLapVoiceType: MyBestLapVoiceType,
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
        val stateWithCurrentBestLap = state.copy(previousBestLapTimeMs = current)
        val previous = state.previousBestLapTimeMs
        if (previous == null) return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (current <= 0) return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (previous > 0 && current >= previous) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (current >= state.personalBestMs) return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (!settings.enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.MyBestLap.Root)) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        val event =
            when (settings.myBestLapVoiceType) {
                MyBestLapVoiceType.FORMAL -> SpeechEvent.Gt7Ps5MyBestLapFormal
                MyBestLapVoiceType.CASUAL -> SpeechEvent.Gt7Ps5MyBestLapCasual
            }
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
            events = listOf(SpeechEvent.RemainingFuelLapsWarning(remainingLaps)),
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
                settings.enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root)
        return Gt7Ps5NarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events = if (shouldAnnounce) listOf(SpeechEvent.Gt7Ps5RemainingFuelWarning) else emptyList(),
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
                settings.enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning)
        return Gt7Ps5NarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events = if (shouldAnnounce) listOf(SpeechEvent.Gt7Ps5TyreOverheat) else emptyList(),
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
                    bestLapTimeMs = telemetry.bestLapTimeMs,
                    totalRefueled = Gt7Ps5FuelUnit(0f),
                    hasRefueled = false,
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }

            else -> {
                val refueled = (telemetry.gasLevel - state.currentGasLevel).coerceAtLeast(Gt7Ps5FuelUnit(0f))
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
                    bestLapTimeMs = telemetry.bestLapTimeMs,
                    totalRefueled = state.totalRefueled + refueled,
                    hasRefueled = refueled > Gt7Ps5FuelUnit(0f),
                    isNewSession = false,
                    observedAtMs = observedAtMs,
                )
            }
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
        if (!settings.enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        return RemainingFuelLapsEvaluation(fuelState.currentLap, remainingLapsFloor)
    }

    private fun isLowRemainingFuel(
        telemetry: Gt7Ps5TelemetryData,
        thresholdPercentage: Int,
    ): Boolean =
        telemetry.gasLevel > Gt7Ps5FuelUnit(0f) &&
            telemetry.gasCapacity > Gt7Ps5FuelUnit(0f) &&
            telemetry.gasLevel.value * 100f <= thresholdPercentage * telemetry.gasCapacity.value

    private companion object {
        const val REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS = 30_000
        const val CURRENT_LAP_CONSUMPTION_WEIGHT = 0.9f
        const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f
    }
}

private data class RemainingFuelLapsEvaluation(
    val evaluatedLap: Int,
    val remainingLaps: Int?,
)
