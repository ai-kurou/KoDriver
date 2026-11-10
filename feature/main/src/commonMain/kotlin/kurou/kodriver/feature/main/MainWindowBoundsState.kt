package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.model.MAIN_WINDOW_POSITION_UNSPECIFIED
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.usecase.ObserveMainWindowBoundsUseCase
import kurou.kodriver.domain.usecase.SaveMainWindowBoundsUseCase
import org.koin.compose.koinInject

/**
 * メインウィンドウの位置・サイズの保存値を購読し、[transform] で呼び出し側の型へ変換して返す。
 * 読み込みが完了するまでは `null` を返す。
 *
 * ウィンドウをホストする `app:desktopApp` と、その橋渡しをする `app:shared` は `:core:domain` を
 * 参照できない（`moduleGraphAssert`）。そのため [MainWindowBounds] をそのまま返さず、[transform] に
 * 位置・サイズを渡して呼び出し側の型へ変換する。位置が未保存の場合は
 * [MAIN_WINDOW_POSITION_UNSPECIFIED] という番兵ではなく `x` / `y` を `null` として渡す。
 *
 * `application` スコープからの利用となり `ViewModelStoreOwner` が無いため、ViewModel を経由せず
 * UseCase を直接 inject する。
 */
@Composable
fun <T> rememberMainWindowBounds(transform: (x: Int?, y: Int?, width: Int, height: Int) -> T): T? {
    val observeMainWindowBounds: ObserveMainWindowBoundsUseCase = koinInject()
    val boundsFlow: Flow<MainWindowBounds> =
        remember(observeMainWindowBounds) { observeMainWindowBounds() }
    val bounds by boundsFlow.collectAsState(initial = null)
    return bounds?.let {
        transform(
            it.x.takeIf { _ -> it.isPositionSpecified },
            it.y.takeIf { _ -> it.isPositionSpecified },
            it.width,
            it.height,
        )
    }
}

/**
 * メインウィンドウの位置・サイズを保存する関数を返す。
 *
 * [rememberMainWindowBounds] と同じ理由で、[MainWindowBounds] ではなく位置・サイズを個別の値として受け取る。
 */
@Composable
fun rememberMainWindowBoundsSaver(): (x: Int, y: Int, width: Int, height: Int) -> Unit {
    val saveMainWindowBounds: SaveMainWindowBoundsUseCase = koinInject()
    val scope = rememberCoroutineScope()
    return remember(saveMainWindowBounds, scope) {
        createMainWindowBoundsSaver(saveMainWindowBounds, scope)
    }
}

internal fun createMainWindowBoundsSaver(
    saveMainWindowBounds: SaveMainWindowBoundsUseCase,
    scope: CoroutineScope,
): (x: Int, y: Int, width: Int, height: Int) -> Unit =
    fun(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        scope.launchCapturingErrors {
            saveMainWindowBounds(MainWindowBounds(x = x, y = y, width = width, height = height))
        }
    }
