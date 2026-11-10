package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateFadeContent
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType

// 表示するフラッグが変わらないテレメトリの更新ではフェードし直さないよう、表示内容だけをキーにする。
internal fun flagInfoContentKey(aceWindowsFlag: AceWindowsFlagData?): AceWindowsFlagType? = aceWindowsFlag?.flag

@Composable
internal fun FlagInfoContent(aceWindowsFlag: AceWindowsFlagData?) {
    DebugStateFadeContent(
        state = aceWindowsFlag,
        contentKey = { flagInfoContentKey(it) },
    ) { flags ->
        AceFlagInfoContent(flags)
    }
}
