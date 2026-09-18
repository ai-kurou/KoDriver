package kurou.kodriver.feature.narratoroverlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OVERLAY_VISIBLE_DEFAULT
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import org.koin.compose.koinInject

/**
 * オーバーレイを表示するかどうかのユーザー設定を購読する。
 *
 * オーバーレイをホストするウィンドウ（`app:desktopApp` の `NarratorOverlayWindow`）は
 * アプリ本体の Window の外側（`application` スコープ）で開くため、`ViewModelStoreOwner` が
 * 供給されず [org.koin.compose.viewmodel.koinViewModel] を使えない。そのため
 * [NarratorOverlayViewModel] を経由せず、UseCase を直接 inject して購読する。
 */
@Composable
fun rememberNarratorOverlayVisible(): Boolean {
    val observeOverlayVisible: ObserveOverlayVisibleUseCase = koinInject()
    val overlayVisible: Flow<Boolean> = remember(observeOverlayVisible) { observeOverlayVisible() }
    val visible by overlayVisible.collectAsState(initial = OVERLAY_VISIBLE_DEFAULT)
    return visible
}
