package kurou.kodriver.feature.narrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NarratorEffect() {
    koinViewModel<NarratorViewModel>()
}
