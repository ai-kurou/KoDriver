package kurou.kodriver.feature.announcer

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnnouncerEffect() {
    koinViewModel<AnnouncerViewModel>()
}
