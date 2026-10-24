package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsRemainingFuelWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

class DetermineLmuWindowsFuelReadoutUseCase {
    /** 燃料を使うクラスでのみ、残量が閾値以下になった瞬間に1回だけ警告する。 */
    fun determineRemainingFuel(
        state: LmuWindowsNarratorState,
        data: LmuWindowsRemainingFuelData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        if (settings.energySource != LmuWindowsEnergySource.Fuel) {
            return LmuWindowsNarratorReadoutDecision(state = state, events = emptyList())
        }
        val isLow = data.remainingPercent <= settings.remainingFuelThresholdPercentage
        val shouldAnnounce =
            !state.remainingFuelWarned &&
                isLow &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingFuel.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events =
                if (shouldAnnounce) {
                    listOf(LmuWindowsRemainingFuelWarning(settings.remainingFuelThresholdPercentage))
                } else {
                    emptyList()
                },
        )
    }
}
