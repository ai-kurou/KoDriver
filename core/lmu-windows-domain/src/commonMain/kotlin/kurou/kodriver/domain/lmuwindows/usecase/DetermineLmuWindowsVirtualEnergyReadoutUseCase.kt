package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

public class DetermineLmuWindowsVirtualEnergyReadoutUseCase {
    public fun determineRemainingVirtualEnergy(
        state: LmuWindowsNarratorState,
        data: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        if (settings.energySource != LmuWindowsEnergySource.VirtualEnergy) {
            return LmuWindowsNarratorReadoutDecision(state, emptyList())
        }
        val remainingPercentage = data.remainingRatio.value * LMU_WINDOWS_PERCENTAGE_SCALE
        val isLow = remainingPercentage <= settings.remainingVirtualEnergyThresholdPercentage
        val shouldAnnounce =
            !state.remainingVirtualEnergyWarned &&
                isLow &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(remainingVirtualEnergyWarned = isLow),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsRemainingVirtualEnergyWarning(
                            settings.remainingVirtualEnergyThresholdPercentage,
                        ),
                    )
                } else {
                    emptyList()
                },
        )
    }
}
