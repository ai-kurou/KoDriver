package kurou.kodriver.feature.gt7ps5narrator

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Gt7Ps5Narrator の副作用を画面表示中に実行する Composable。
 */
@Composable
fun Gt7Ps5NarratorEffect() {
    koinViewModel<Gt7Ps5NarratorViewModel>()
}
