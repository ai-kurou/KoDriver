package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateFadeContent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData

// 表示するフラッグが変わらないテレメトリの更新ではフェードし直さないよう、表示内容だけをキーにする。
internal fun flagInfoContentKey(raceFlags: LmuWindowsRaceFlagsData?): List<ActiveRaceFlag>? =
    raceFlags?.let { determineActiveRaceFlags(it) }

@Composable
internal fun FlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    DebugStateFadeContent(
        state = raceFlags,
        contentKey = { flagInfoContentKey(it) },
    ) { flags ->
        LmuFlagInfoContent(flags)
    }
}
