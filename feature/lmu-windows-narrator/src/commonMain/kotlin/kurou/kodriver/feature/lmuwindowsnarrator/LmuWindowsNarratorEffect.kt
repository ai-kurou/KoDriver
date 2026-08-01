package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsNarrator の副作用を画面表示中に実行する Composable。
 */
@Composable
fun LmuWindowsNarratorEffect() {
    koinViewModel<LmuWindowsNarratorViewModel>()
}
