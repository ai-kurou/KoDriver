package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData

// 表示するフラッグが変わらないテレメトリの更新ではフェードし直さないよう、表示内容だけをキーにする。
internal fun flagInfoContentKey(raceFlags: LmuWindowsRaceFlagsData?): List<ActiveRaceFlag>? =
    raceFlags?.let { determineActiveRaceFlags(it) }

@Composable
internal fun FlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    AnimatedContent(
        targetState = raceFlags,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { flagInfoContentKey(it) },
    ) { flags ->
        LmuFlagInfoContent(flags)
    }
}
