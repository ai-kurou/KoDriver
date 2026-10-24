package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

class DetermineLmuWindowsVirtualEnergyReadoutUseCase(
    private val manualReadoutUseCase: DetermineLmuWindowsManualReadoutUseCase =
        DetermineLmuWindowsManualReadoutUseCase(),
) {
    fun determineRemainingVirtualEnergy(
        state: LmuWindowsNarratorState,
        data: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
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

    /** 警告の閾値・読み上げ済み状態を変更せず、現在の残量を読み上げる。 */
    fun determineRemainingVirtualEnergyCurrentValue(
        state: LmuWindowsNarratorState,
        data: LmuWindowsVirtualEnergyData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val shouldAnnounce =
            settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout)
        return if (shouldAnnounce) {
            manualReadoutUseCase.determineManualRemainingVirtualEnergyCurrentValue(state, data)
        } else {
            LmuWindowsNarratorReadoutDecision(state = state, events = emptyList())
        }
    }
}
