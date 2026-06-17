package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsNarratorEffect() {
    koinViewModel<LmuWindowsNarratorViewModel>()
}
