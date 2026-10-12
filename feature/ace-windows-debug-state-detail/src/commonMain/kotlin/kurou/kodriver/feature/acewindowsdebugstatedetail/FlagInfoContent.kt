package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType

// 表示するフラッグが変わらないテレメトリの更新ではフェードし直さないよう、表示内容だけをキーにする。
internal fun flagInfoContentKey(aceWindowsFlag: AceWindowsFlagData?): AceWindowsFlagType? = aceWindowsFlag?.flag

@Composable
internal fun FlagInfoContent(aceWindowsFlag: AceWindowsFlagData?) {
    AnimatedContent(
        targetState = aceWindowsFlag,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { flagInfoContentKey(it) },
    ) { flags ->
        AceFlagInfoContent(flags)
    }
}
