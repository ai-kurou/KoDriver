package kurou.kodriver.feature.debugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.Simulator

// 表示するフラグが変わらないテレメトリの更新ではフェードし直さないよう、表示内容だけをキーにする。
internal fun flagInfoContentKey(
    selectedSimulator: Simulator,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
): Pair<Simulator, Any?> =
    selectedSimulator to
        when (selectedSimulator) {
            is Simulator.LmuWindows -> raceFlags?.let { determineActiveRaceFlags(it) }
            is Simulator.AceWindows -> aceWindowsFlag?.flag
            is Simulator.Gt7Ps5 -> null
        }

@Composable
internal fun FlagInfoContent(
    selectedSimulator: Simulator,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
) {
    AnimatedContent(
        targetState = Triple(selectedSimulator, raceFlags, aceWindowsFlag),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { (simulator, lmuFlags, aceFlag) -> flagInfoContentKey(simulator, lmuFlags, aceFlag) },
    ) { (simulator, lmuFlags, aceFlag) ->
        FlagInfoStaticContent(simulator, lmuFlags, aceFlag)
    }
}

@Composable
private fun FlagInfoStaticContent(
    selectedSimulator: Simulator,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> {
            LmuFlagInfoContent(raceFlags)
        }

        is Simulator.AceWindows -> {
            AceFlagInfoContent(aceWindowsFlag)
        }

        is Simulator.Gt7Ps5 -> {
            DebugStateUnavailableContent()
        }
    }
}
