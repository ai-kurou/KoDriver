package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * ACE 向け読み上げ判定の継続状態。
 *
 * 同じ低燃料警告や同じ旗状態を連続で読み上げないため、前回の判定結果を保持する。
 */
data class AceWindowsNarratorState(
    val remainingFuelWarned: Boolean = false,
    val previousFlag: AceWindowsFlagType? = null,
)

/** ACE 向け読み上げ判定で参照するユーザー設定。 */
data class AceWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val remainingFuelThresholdPercentage: Int,
)

/** ACE 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class AceWindowsNarratorReadoutDecision(
    val state: AceWindowsNarratorState,
    val events: List<SpeechEvent>,
)

/**
 * ACE の燃料残量・旗状態から、今回読み上げるべき音声イベントを決定する UseCase。
 */
class DetermineAceWindowsNarratorReadoutUseCase {
    fun determineRemainingFuel(
        state: AceWindowsNarratorState,
        data: AceWindowsFuelData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val isLow = data.remainingPercent > 0.0 && data.remainingPercent <= settings.remainingFuelThresholdPercentage
        val shouldAnnounce = !state.remainingFuelWarned && isLow &&
            settings.enabledStates.getValue(ReadoutItemKey.AceWindows.RemainingFuel.Root)
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
        if (!settings.enabledStates.getValue(ReadoutItemKey.AceWindows.Flag.Root)) {
            return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        }
        if (data.flag == previous) return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        val event = flagEvent(data.flag)
            ?.takeIf { (itemKey, _) ->
            settings.enabledStates.getValue(itemKey)
        }?.second
        return AceWindowsNarratorReadoutDecision(nextState, listOfNotNull(event))
    }

    private fun flagEvent(flag: AceWindowsFlagType): Pair<ReadoutItemKey, SpeechEvent>? = when (flag) {
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
}
