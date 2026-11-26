package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.determineHysteresisState
import kotlin.math.roundToInt

private const val TYRE_LOW_WARNING_THRESHOLD_CELSIUS = 60f
private const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f
private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

public class DetermineLmuWindowsTyreReadoutUseCase {
    public fun determineTyreTemperatureOverheat(
        state: LmuWindowsNarratorState,
        input: TyreTemperatureReadoutInput,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val data = input.tyreCarcassTemperature
        // NaN・無限大は判定と最大値の対象から外す（NaN は roundToInt で例外になる）。
        val finiteCelsius =
            data.wheels.values
                .map { it.value }
                .filter { it.isFinite() }
        val hotThreshold = settings.tyreTemperatureHighThresholdCelsius.value.toFloat()
        val nextOverheating =
            determineHysteresisState(
                valuesCelsius = finiteCelsius,
                highThresholdCelsius = hotThreshold,
                hysteresisCelsius = TYRE_OVERHEAT_HYSTERESIS_CELSIUS,
                currentState = state.tyreOverheating,
            )
        val shouldAnnounce =
            !state.tyreOverheating && nextOverheating &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsTyreOverheat(celsius = finiteCelsius.max().roundToInt()),
                    )
                } else {
                    emptyList()
                },
        )
    }

    public fun determineTyreTemperatureLow(
        state: LmuWindowsNarratorState,
        input: TyreTemperatureReadoutInput,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val data = input.tyreCarcassTemperature
        val raceFlags = input.raceFlags
        // 低温が続く間は繰り返さず、選択フェーズへ入った瞬間だけ警告する。初回受信は遷移とみなさない。
        val previousGamePhase = state.previousGamePhaseForTyreLowWarning
        val enteringTargetPhase =
            previousGamePhase != null &&
                raceFlags.gamePhase != previousGamePhase &&
                raceFlags.gamePhase in settings.tyreTemperatureLowWarningPhases
        val finiteCelsius =
            data.wheels.values
                .map { it.value }
                .filter { it.isFinite() }
        val anyCold = finiteCelsius.any { it <= TYRE_LOW_WARNING_THRESHOLD_CELSIUS }
        val shouldAnnounce =
            enteringTargetPhase && anyCold &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.LowWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousGamePhaseForTyreLowWarning = raceFlags.gamePhase),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsTyreCold(celsius = finiteCelsius.max().roundToInt()),
                    )
                } else {
                    emptyList()
                },
        )
    }

    public fun determineTyreWear(
        state: LmuWindowsNarratorState,
        data: LmuWindowsTyreWearData,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val anyWorn =
            data.wheels.values.any { remainingRatio ->
                remainingRatio.value * LMU_WINDOWS_PERCENTAGE_SCALE <= settings.tyreWearThresholdPercentage
            }
        val shouldAnnounce =
            !state.tyreWearWarned && anyWorn &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreWear.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreWear.WarningReadout)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreWearWarned = anyWorn),
            events =
                if (shouldAnnounce) {
                    // 自動摩耗警告の文言には実測残量ではなく、越えた設定閾値を渡す（手動の現在値とは異なる）。
                    listOf(LmuWindowsTyreWearWarning(percentage = settings.tyreWearThresholdPercentage))
                } else {
                    emptyList()
                },
        )
    }
}
