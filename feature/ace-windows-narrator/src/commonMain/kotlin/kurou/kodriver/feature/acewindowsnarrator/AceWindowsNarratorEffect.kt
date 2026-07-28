package kurou.kodriver.feature.acewindowsnarrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AceWindowsNarratorEffect() {
    koinViewModel<AceWindowsNarratorViewModel>()
}
