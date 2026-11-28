package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

public class DetermineLmuWindowsMyBestLapReadoutUseCase {
    public fun determineMyBestLap(
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
        // 読み上げの有無にかかわらず観測した最速値を記録し、0からの復帰を更新と誤認しないようにする。
        val personalBestMs =
            if (current > 0L) minOf(sessionState.personalBestMs, current) else sessionState.personalBestMs
        val observedState =
            sessionState.copy(
                personalBestMs = personalBestMs,
                previousBestLapTimeMs = current,
                previousLapCount = currentLap,
            )
        // 初回観測の値は、このアプリが更新を確認したものではないため読み上げない。
        if (sessionState.previousBestLapTimeMs == null) {
            return LmuWindowsNarratorReadoutDecision(observedState, emptyList())
        }
        if (current <= 0L || current >= sessionState.personalBestMs) {
            return LmuWindowsNarratorReadoutDecision(observedState, emptyList())
        }
        if (!settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.Root) ||
            !settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled)
        ) {
            return LmuWindowsNarratorReadoutDecision(observedState, emptyList())
        }

        val event = LmuWindowsMyBestLap(lapTimeMs = current)
        return LmuWindowsNarratorReadoutDecision(
            state = observedState,
            events = listOf(event),
        )
    }
}
