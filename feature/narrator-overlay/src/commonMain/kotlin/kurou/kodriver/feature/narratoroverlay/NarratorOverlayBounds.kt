package kurou.kodriver.feature.narratoroverlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.OVERLAY_WINDOW_POSITION_UNSPECIFIED
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.usecase.ObserveOverlayWindowBoundsUseCase
import kurou.kodriver.domain.usecase.SaveOverlayWindowBoundsUseCase
import org.koin.compose.koinInject

/**
 * オーバーレイウィンドウの位置・サイズの保存値を購読し、[transform] で呼び出し側の型へ変換して返す。
 * 読み込みが完了するまでは `null` を返す。
 *
 * [rememberNarratorOverlayVisible] と同じ理由で、読み込み前を `null` で表現する。保存値より先に
 * 既定位置でウィンドウを生成してしまうと、復元位置へ移動する様子がユーザーに見えてしまうため、
 * 呼び出し側は値が揃うまでウィンドウを生成しない。
 *
 * ウィンドウをホストする `app:desktopApp` と、その橋渡しをする `app:shared` は `:core:domain` を
 * 参照できない（`moduleGraphAssert` の `:app:.*App -X> :core:domain` / `:app:shared -X> :core:.*`）。
 * そのため [OverlayWindowBounds] をそのまま返さず、[transform] に位置・サイズを渡して呼び出し側の
 * 型へ変換する。位置が未保存の場合は [OVERLAY_WINDOW_POSITION_UNSPECIFIED] という番兵ではなく
 * `x` / `y` を `null` として渡し、呼び出し側が番兵の値を知らなくて済むようにする。
 *
 * `application` スコープからの利用となり `ViewModelStoreOwner` が無いため、ViewModel を経由せず
 * UseCase を直接 inject する（[rememberNarratorOverlayVisible] の KDoc を参照）。
 */
@Composable
fun <T> rememberNarratorOverlayBounds(transform: (x: Int?, y: Int?, width: Int, height: Int) -> T): T? {
    val observeOverlayWindowBounds: ObserveOverlayWindowBoundsUseCase = koinInject()
    val boundsFlow: Flow<OverlayWindowBounds> =
        remember(observeOverlayWindowBounds) { observeOverlayWindowBounds() }
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
 * オーバーレイウィンドウの位置・サイズを保存する関数を返す。
 *
 * [rememberNarratorOverlayBounds] と同じ理由で、[OverlayWindowBounds] ではなく位置・サイズを
 * 個別の値として受け取る。
 */
@Composable
fun rememberNarratorOverlayBoundsSaver(): (x: Int, y: Int, width: Int, height: Int) -> Unit {
    val saveOverlayWindowBounds: SaveOverlayWindowBoundsUseCase = koinInject()
    val scope = rememberCoroutineScope()
    return remember(saveOverlayWindowBounds, scope) {
        createOverlayWindowBoundsSaver(saveOverlayWindowBounds, scope)
    }
}

private fun createOverlayWindowBoundsSaver(
    saveOverlayWindowBounds: SaveOverlayWindowBoundsUseCase,
    scope: CoroutineScope,
): (x: Int, y: Int, width: Int, height: Int) -> Unit =
    fun(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        scope.launch {
            saveOverlayWindowBounds(OverlayWindowBounds(x = x, y = y, width = width, height = height))
        }
    }
