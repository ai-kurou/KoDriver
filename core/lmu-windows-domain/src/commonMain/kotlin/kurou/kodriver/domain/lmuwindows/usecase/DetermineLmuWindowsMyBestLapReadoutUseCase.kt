package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

class DetermineLmuWindowsMyBestLapReadoutUseCase {
    fun determineMyBestLap(
        state: LmuWindowsNarratorState,
        telemetry: LmuWindowsTelemetryData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val current = telemetry.timing.bestLapTimeMs
        val currentLap = telemetry.timing.currentLap
        val sessionState =
            if (state.previousLapCount != null && currentLap < state.previousLapCount) {
                state.copy(personalBestMs = Long.MAX_VALUE, previousBestLapTimeMs = null)
            } else {
                state
            }
        val stateWithCurrentBestLap = sessionState.copy(previousBestLapTimeMs = current, previousLapCount = currentLap)
        val previous = sessionState.previousBestLapTimeMs
        if (previous == null) return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (current <= 0L) return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (previous > 0L && current >= previous) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (current >= sessionState.personalBestMs) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.Root) ||
            !settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled)
        ) {
            return LmuWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        val event = LmuWindowsMyBestLap(lapTimeMs = current)
        return LmuWindowsNarratorReadoutDecision(
            state = stateWithCurrentBestLap.copy(personalBestMs = current),
            events = listOf(event),
        )
    }
}
