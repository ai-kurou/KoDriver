package kurou.kodriver.feature.desktopsplash

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 起動中の初期化進捗を保持する状態ホルダー。
 *
 * main.kt 側の起動オーケストレーションが [update] で現在のフェーズを更新し、
 * スプラッシュ画面は [uiState] を購読して表示する。ViewModel のように
 * コルーチンで外部ソースを購読するわけではなく、外部（main.kt）から
 * 駆動される揮発的な状態のため、単純な [MutableStateFlow] ホルダーとする。
 */
class DesktopSplashProgress {

    private val _uiState = MutableStateFlow(DesktopSplashUiState())

    /** スプラッシュ画面が購読する現在の表示状態。 */
    val uiState: StateFlow<DesktopSplashUiState> = _uiState.asStateFlow()

    /** 現在の初期化フェーズを [step] に更新する。 */
    fun update(step: DesktopSplashStep) {
        _uiState.update { it.copy(step = step) }
    }
}
