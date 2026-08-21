package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/**
 * ACE 向け読み上げ判定の継続状態。
 *
 * 同じ低燃料警告・同じ旗状態・同じタイヤ過熱状態を連続で読み上げないため、前回の判定結果を保持する。
 */
data class AceWindowsNarratorState(
    val remainingFuelWarned: Boolean = false,
    val previousFlag: AceWindowsFlagType? = null,
    val tyreOverheating: Boolean = false,
)

/** ACE 向け読み上げ判定で参照するユーザー設定。 */
data class AceWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val remainingFuelThresholdPercentage: Int,
    val tyreTemperatureHighThresholdCelsius: Celsius = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
)

/** ACE 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class AceWindowsNarratorReadoutDecision(
    val state: AceWindowsNarratorState,
    val events: List<SpeechEvent>,
)

/**
 * ACE の燃料残量・旗状態・タイヤカーカス温度から、今回読み上げるべき音声イベントを決定する UseCase。
 *
 * 注意: [ReadoutItemKey.AceWindows.VehicleApproach.Root] は listPane には表示されるが、
 * 車両接近アナウンスの実際の読み上げ判定はこの UseCase にまだ配線されていない（別PRで対応予定）。
 * detailPane・DataStoreへの永続化も未実装のため、現状はlistPaneのスイッチ操作が読み上げに反映されない。
 */
class DetermineAceWindowsNarratorReadoutUseCase {
    fun determineRemainingFuel(
        state: AceWindowsNarratorState,
        data: AceWindowsFuelData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val isLow =
            data.remainingPercent.value > 0.0 &&
                data.remainingPercent.value <= settings.remainingFuelThresholdPercentage
        val shouldAnnounce =
            !state.remainingFuelWarned && isLow &&
                settings.enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.RemainingFuel.Root)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events = if (shouldAnnounce) listOf(SpeechEvent.AceWindowsRemainingFuelWarning) else emptyList(),
        )
    }

    fun determineFlag(
        state: AceWindowsNarratorState,
        data: AceWindowsFlagData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val previous = state.previousFlag
        val nextState = state.copy(previousFlag = data.flag)
        if (previous == null) return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        if (!settings.enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.Flag.Root)) {
            return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        }
        if (data.flag == previous) return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        val event =
            flagEvent(data.flag)
                ?.takeIf { (itemKey, _) ->
                    settings.enabledStates.readoutEnabled(itemKey)
                }?.second
        return AceWindowsNarratorReadoutDecision(nextState, listOfNotNull(event))
    }

    fun determineTyreTemperatureOverheat(
        state: AceWindowsNarratorState,
        data: AceWindowsTyreCarcassTemperatureData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
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
                settings.enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events = if (shouldAnnounce) listOf(SpeechEvent.AceWindowsTyreOverheat) else emptyList(),
        )
    }

    private fun flagEvent(flag: AceWindowsFlagType): Pair<ReadoutItemKey, SpeechEvent>? =
        when (flag) {
            AceWindowsFlagType.WHITE_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.WhiteFlag to SpeechEvent.AceWindowsWhiteFlag
            }

            AceWindowsFlagType.GREEN_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.GreenFlag to SpeechEvent.AceWindowsGreenFlag
            }

            AceWindowsFlagType.RED_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.RedFlag to SpeechEvent.AceWindowsRedFlag
            }

            AceWindowsFlagType.BLUE_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.BlueFlag to SpeechEvent.AceWindowsBlueFlag
            }

            AceWindowsFlagType.YELLOW_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.YellowFlag to SpeechEvent.AceWindowsYellowFlag
            }

            AceWindowsFlagType.BLACK_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.BlackFlag to SpeechEvent.AceWindowsBlackFlag
            }

            AceWindowsFlagType.BLACK_WHITE_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to SpeechEvent.AceWindowsBlackWhiteFlag
            }

            AceWindowsFlagType.CHECKERED_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.CheckeredFlag to SpeechEvent.AceWindowsCheckeredFlag
            }

            AceWindowsFlagType.ORANGE_CIRCLE_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to SpeechEvent.AceWindowsOrangeCircleFlag
            }

            AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG -> {
                ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to SpeechEvent.AceWindowsRedYellowStripesFlag
            }

            AceWindowsFlagType.NO_FLAG, AceWindowsFlagType.UNKNOWN -> {
                null
            }
        }

    private companion object {
        const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f
    }
}
