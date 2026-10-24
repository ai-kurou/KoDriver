package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled
import kotlin.math.roundToInt

private const val TYRE_LOW_WARNING_THRESHOLD_CELSIUS = 60f
private const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f
private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

class DetermineLmuWindowsTyreReadoutUseCase {
    fun determineTyreTemperatureOverheat(
        state: LmuWindowsNarratorState,
        input: TyreTemperatureReadoutInput,
        settings: LmuWindowsNarratorReadoutSettings,
    ): LmuWindowsNarratorReadoutDecision {
        val data = input.tyreCarcassTemperature
        val hotThreshold = settings.tyreTemperatureHighThresholdCelsius.value.toFloat()
        val coolThreshold = hotThreshold - TYRE_OVERHEAT_HYSTERESIS_CELSIUS
        val anyHot = data.wheels.values.any { it.value >= hotThreshold }
        val allCool = data.wheels.values.all { it.value <= coolThreshold }
        val nextOverheating =
            when {
                anyHot -> true
                allCool -> false
                else -> state.tyreOverheating
            }
        val shouldAnnounce =
            !state.tyreOverheating && nextOverheating &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsTyreOverheat(
                            celsius =
                                data.wheels.values
                                    .maxOf { it.value }
                                    .roundToInt(),
                        ),
                    )
                } else {
                    emptyList()
                },
        )
    }

    fun determineTyreTemperatureLow(
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
        val anyCold = data.wheels.values.any { it.value <= TYRE_LOW_WARNING_THRESHOLD_CELSIUS }
        val shouldAnnounce =
            enteringTargetPhase && anyCold &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.TyreTemperature.LowWarning)
        return LmuWindowsNarratorReadoutDecision(
            state = state.copy(previousGamePhaseForTyreLowWarning = raceFlags.gamePhase),
            events =
                if (shouldAnnounce) {
                    listOf(
                        LmuWindowsTyreCold(
                            celsius =
                                data.wheels.values
                                    .maxOf { it.value }
                                    .roundToInt(),
                        ),
                    )
                } else {
                    emptyList()
                },
        )
    }

    fun determineTyreWear(
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
