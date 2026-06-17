package kurou.kodriver.feature.lmunarrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsNarratorEffect() {
    koinViewModel<LmuWindowsNarratorViewModel>()
}
