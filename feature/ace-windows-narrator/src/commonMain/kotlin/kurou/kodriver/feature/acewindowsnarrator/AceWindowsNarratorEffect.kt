package kurou.kodriver.feature.acewindowsnarrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

/**
 * AceWindowsNarrator の副作用を画面表示中に実行する Composable。
 */
@Composable
fun AceWindowsNarratorEffect() {
    koinViewModel<AceWindowsNarratorViewModel>()
}
