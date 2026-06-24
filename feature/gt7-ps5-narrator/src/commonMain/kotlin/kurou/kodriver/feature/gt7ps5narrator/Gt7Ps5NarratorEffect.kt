package kurou.kodriver.feature.gt7ps5narrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Gt7Ps5NarratorEffect() {
    koinViewModel<Gt7Ps5NarratorViewModel>()
}
