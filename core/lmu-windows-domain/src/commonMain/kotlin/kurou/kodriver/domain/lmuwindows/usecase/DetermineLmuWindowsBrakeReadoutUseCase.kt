package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.determineHysteresisState

private const val BRAKE_TEMPERATURE_OVERHEAT_HYSTERESIS_CELSIUS = 100f

class DetermineLmuWindowsBrakeReadoutUseCase {
    fun determineBrakeTemperatureOverheat(
        state: LmuWindowsNarratorState,
        data: LmuWindowsBrakeTemperatureData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val hotThreshold = settings.brakeTemperatureHighThresholdCelsius.value.toFloat()
        val nextOverheating =
            determineHysteresisState(
                valuesCelsius = data.wheels.values.map { it.value },
                highThresholdCelsius = hotThreshold,
                hysteresisCelsius = BRAKE_TEMPERATURE_OVERHEAT_HYSTERESIS_CELSIUS,
                currentState = state.brakeOverheating,
            )
        val shouldAnnounce =
            !state.brakeOverheating && nextOverheating &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeTemperature.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(brakeOverheating = nextOverheating),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsBrakeOverheat(
                            celsius = settings.brakeTemperatureHighThresholdCelsius.value,
                        ),
                    )
                } else {
                    emptyList()
                },
        )
    }

    fun determineBrakeWearLow(
        state: LmuWindowsNarratorState,
        data: LmuWindowsBrakeWearRemainingData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val anyLow =
            data.wheels.values.any { it.remainingPercent <= settings.brakeWearLowThresholdPercent }
        val shouldAnnounce =
            !state.brakeWearWarned && anyLow &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeWear.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeWear.WarningReadout)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(brakeWearWarned = anyLow),
            events =
                if (shouldAnnounce) {
                    listOf(LmuWindowsBrakeWearLow(percent = settings.brakeWearLowThresholdPercent))
                } else {
                    emptyList()
                },
        )
    }
}
