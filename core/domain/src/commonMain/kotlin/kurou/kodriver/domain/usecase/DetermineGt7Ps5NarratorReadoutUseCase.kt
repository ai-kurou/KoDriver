package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey

data class Gt7Ps5NarratorState(
    val personalBestMs: Int = Int.MAX_VALUE,
    val previousBestLapTimeMs: Int? = null,
    val lastAnnouncedRemainingLaps: Int = -1,
    val lastFuelEvaluationLap: Int = -1,
    val fuelTrackingState: Gt7Ps5FuelTrackingState = Gt7Ps5FuelTrackingState(),
)

data class Gt7Ps5FuelTrackingState(
    val raceStartFuel: Float? = null,
    val raceStartLap: Int? = null,
    val currentLap: Int = -1,
    val currentLapStartedAtMs: Long = 0L,
    val currentGasLevel: Float = 0f,
    val bestLapTimeMs: Int = -1,
    val totalRefueled: Float = 0f,
    val isNewSession: Boolean = false,
    val observedAtMs: Long = 0L,
)

data class Gt7Ps5NarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val myBestLapVoiceType: MyBestLapVoiceType,
    val remainingFuelLapsThreshold: Int,
    val remainingFuelLapsEnabled: Boolean,
)

data class Gt7Ps5NarratorReadoutDecision(
    val state: Gt7Ps5NarratorState,
    val events: List<SpeechEvent>,
)

class DetermineGt7Ps5NarratorReadoutUseCase {
    operator fun invoke(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
        observedAtMs: Long,
    ): Gt7Ps5NarratorReadoutDecision {
        val myBestLapResult = determineMyBestLap(state, telemetry, settings)
        val fuelResult = determineRemainingFuelLaps(myBestLapResult.state, telemetry, settings, observedAtMs)
        return Gt7Ps5NarratorReadoutDecision(
            state = fuelResult.state,
            events = myBestLapResult.events + fuelResult.events,
        )
    }

    private fun determineMyBestLap(
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
        if (settings.enabledStates[ReadoutItemKey.MyBestLap] == false) {
            return Gt7Ps5NarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        val event = when (settings.myBestLapVoiceType) {
            MyBestLapVoiceType.FORMAL -> SpeechEvent.MyBestLapFormal
            MyBestLapVoiceType.CASUAL -> SpeechEvent.MyBestLapCasual
        }
        return Gt7Ps5NarratorReadoutDecision(
            state = stateWithCurrentBestLap.copy(personalBestMs = current),
            events = listOf(event),
        )
    }

    private fun determineRemainingFuelLaps(
        state: Gt7Ps5NarratorState,
        telemetry: Gt7Ps5TelemetryData,
        settings: Gt7Ps5NarratorReadoutSettings,
        observedAtMs: Long,
    ): Gt7Ps5NarratorReadoutDecision {
        val fuelTrackingState = trackFuel(state.fuelTrackingState, telemetry, observedAtMs)
        val stateAfterTracking = if (fuelTrackingState.isNewSession) {
            state.copy(
                lastAnnouncedRemainingLaps = -1,
                lastFuelEvaluationLap = -1,
                fuelTrackingState = fuelTrackingState,
            )
        } else {
            state.copy(fuelTrackingState = fuelTrackingState)
        }
        val evaluation = calculateRemainingFuelLaps(stateAfterTracking, settings)
        val stateAfterEvaluation = stateAfterTracking.copy(lastFuelEvaluationLap = evaluation.evaluatedLap)
        val remainingLaps = evaluation.remainingLaps ?: return Gt7Ps5NarratorReadoutDecision(
            stateAfterEvaluation,
            emptyList(),
        )
        return Gt7Ps5NarratorReadoutDecision(
            state = stateAfterEvaluation.copy(lastAnnouncedRemainingLaps = remainingLaps),
            events = listOf(SpeechEvent.RemainingFuelLapsWarning(remainingLaps)),
        )
    }

    private fun trackFuel(
        state: Gt7Ps5FuelTrackingState,
        telemetry: Gt7Ps5TelemetryData,
        observedAtMs: Long,
    ): Gt7Ps5FuelTrackingState =
        when {
            telemetry.lapCount < state.currentLap -> Gt7Ps5FuelTrackingState(
                raceStartFuel = telemetry.gasLevel,
                raceStartLap = telemetry.lapCount,
                currentLap = telemetry.lapCount,
                currentLapStartedAtMs = observedAtMs,
                currentGasLevel = telemetry.gasLevel,
                bestLapTimeMs = telemetry.bestLapTimeMs,
                totalRefueled = 0f,
                isNewSession = true,
                observedAtMs = observedAtMs,
            )
            state.raceStartFuel == null -> Gt7Ps5FuelTrackingState(
                raceStartFuel = telemetry.gasLevel,
                raceStartLap = telemetry.lapCount,
                currentLap = telemetry.lapCount,
                currentLapStartedAtMs = observedAtMs,
                currentGasLevel = telemetry.gasLevel,
                bestLapTimeMs = telemetry.bestLapTimeMs,
                totalRefueled = 0f,
                isNewSession = false,
                observedAtMs = observedAtMs,
            )
            else -> {
                val refueled = (telemetry.gasLevel - state.currentGasLevel).coerceAtLeast(0f)
                val currentLapStartedAtMs = if (telemetry.lapCount != state.currentLap) {
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
        if (fuelState.currentLap == state.lastFuelEvaluationLap) return RemainingFuelLapsEvaluation(
            evaluatedLap = state.lastFuelEvaluationLap,
            remainingLaps = null,
        )
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
        if (consumedFuel <= 0f) return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        val avgConsumption = consumedFuel / (lapsCompleted + CURRENT_LAP_CONSUMPTION_WEIGHT)
        val remainingLapsFloor = (fuelState.currentGasLevel / avgConsumption).toInt()
        if (remainingLapsFloor < 0 || remainingLapsFloor > settings.remainingFuelLapsThreshold) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        if (remainingLapsFloor == state.lastAnnouncedRemainingLaps) {
            return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        }
        if (!settings.remainingFuelLapsEnabled) return RemainingFuelLapsEvaluation(fuelState.currentLap, null)
        return RemainingFuelLapsEvaluation(fuelState.currentLap, remainingLapsFloor)
    }

    private companion object {
        const val REMAINING_FUEL_LAPS_READOUT_BEFORE_BEST_LAP_MS = 30_000
        const val CURRENT_LAP_CONSUMPTION_WEIGHT = 0.9f
    }
}

private data class RemainingFuelLapsEvaluation(
    val evaluatedLap: Int,
    val remainingLaps: Int?,
)
