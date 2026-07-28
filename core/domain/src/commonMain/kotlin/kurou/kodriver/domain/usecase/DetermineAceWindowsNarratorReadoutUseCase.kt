package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.ReadoutItemKey

data class AceWindowsNarratorState(
    val remainingFuelWarned: Boolean = false,
)

data class AceWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val remainingFuelThresholdPercentage: Int,
)

data class AceWindowsNarratorReadoutDecision(
    val state: AceWindowsNarratorState,
    val events: List<SpeechEvent>,
)

class DetermineAceWindowsNarratorReadoutUseCase {
    fun determineRemainingFuel(
        state: AceWindowsNarratorState,
        data: AceWindowsFuelData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val isLow = data.remainingPercent <= settings.remainingFuelThresholdPercentage
        val shouldAnnounce = !state.remainingFuelWarned && isLow &&
            settings.enabledStates.getValue(ReadoutItemKey.AceWindows.RemainingFuel.Root)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events = if (shouldAnnounce) listOf(SpeechEvent.AceWindowsRemainingFuelWarning) else emptyList(),
        )
    }
}
