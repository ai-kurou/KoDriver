package kurou.kodriver

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.presentation.NarratorOverlayScreen
import kurou.kodriver.presentation.NarratorOverlayWindowBounds
import kurou.kodriver.presentation.rememberNarratorOverlayBounds
import kurou.kodriver.presentation.rememberNarratorOverlayBoundsSaver
import kurou.kodriver.presentation.rememberNarratorOverlayVisible
import java.awt.Dimension
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window
import kotlin.math.roundToInt

/**
 * 位置・サイズの変更を保存するまでの待ち時間。ドラッグ・リサイズ中は座標が連続で変化するため、
 * 操作が落ち着いてから一度だけ DataStore へ書き込む。
 */
private const val BOUNDS_SAVE_DEBOUNCE_MILLIS = 500L

/**
 * [WindowState] から取り出した、オーバーレイウィンドウの現在の位置とサイズ（AWT のウィンドウ座標系）。
 */
private data class NarratorOverlayWindowGeometry(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

private val NARRATOR_OVERLAY_MIN_SIZE = DpSize(200.dp, 80.dp)
private val NARRATOR_OVERLAY_MAX_SIZE = DpSize(1200.dp, 600.dp)

/**
 * ゲーム画面に重ねて表示するオーバーレイ専用ウィンドウ（デスクトップアプリのみ、常時表示）。
 *
 * - `WindowDecoration.Undecorated()` かつ `resizable = true` の組み合わせで、Compose Multiplatform 独自の
 *   リサイズハンドル（[androidx.compose.ui.window.UndecoratedWindowResizer]）が有効になる。
 * - パネル全体のドラッグで移動できるよう、[detectDragGestures] でウィンドウ位置を直接更新する。
 *   [androidx.compose.foundation.gestures.PointerInputChange] の`dragAmount`はウィンドウ内のローカル座標
 *   系での差分であり、これを毎フレーム加算してウィンドウを動かすと、ウィンドウの移動そのものが次フレームの
 *   ローカル座標の基準をずらしてしまい、位置計算がフィードバックループを起こしてガタつく（実機で確認済み）。
 *   そのため、ドラッグ開始時のウィンドウ位置とポインタのスクリーン座標（[MouseInfo.getPointerInfo]）を基準に
 *   保持し、以降は「現在のポインタのスクリーン座標との差分」から絶対位置としてウィンドウ位置を計算する。
 * - タスクバーに表示させないため `Window.Type.UTILITY` を設定する。`java.awt.Window#setType` は
 *   ウィンドウが displayable になった後に呼ぶと `IllegalComponentStateException` を送出するため、
 *   通常の `Window`composable の content 内 `LaunchedEffect` から設定すると、環境によっては
 *   既に displayable になっていて例外が発生しうる（実機で確認済み）。[SwingWindow] の `init` は
 *   `ComposeWindow` 生成直後・displayable になる前に一度だけ呼ばれることが保証されているため、
 *   最小/最大サイズの設定とあわせてここで行う。
 * - 表示ON/OFFはその他タブの「オーバーレイ設定」で切り替える（[NarratorOverlayWindowHost] を参照）。
 * - 位置・サイズは [initialBounds] で復元し、変更を [onBoundsChange] で通知する（永続化は
 *   [NarratorOverlayWindowHost] が行う）。`window.location` を直接書き換えるドラッグ移動も、
 *   [SwingWindow] が AWT の `componentMoved` / `componentResized` を [WindowState] へ反映するため、
 *   [WindowState] を監視するだけで移動・リサイズの両方を拾える。
 * - 常時最前面の詳細な制御（フォーカス連動等）は別PRで対応する。
 * - `alwaysOnTop = true` により通常のウィンドウより手前には表示されるが、排他的フルスクリーンで
 *   起動したゲームには他の常駐オーバーレイツールと同様に表示されないため、LMU 側をボーダーレス
 *   ウィンドウモードで起動する前提となる。
 * - ゲーム画面をなるべく隠さないよう `transparent = true` でウィンドウ背景を透過させ、コンテンツ側
 *   （[NarratorOverlayScreen]）の半透明な背景色と組み合わせている。`isTransparent`（`transparent`
 *   パラメータの実体）はウィンドウが表示済みの状態で変更すると例外を送出するが、`SwingWindow` の
 *   `update` ブロックは値に変化がない限り再設定を行わないため、固定値 `true` を渡す限りは
 *   ウィンドウがまだ displayable になる前の初回 `update` 呼び出し時にのみ設定される。
 */
@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
fun ApplicationScope.NarratorOverlayWindow(
    initialBounds: NarratorOverlayWindowBounds,
    onBoundsChange: (x: Int, y: Int, width: Int, height: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowState =
        rememberWindowState(
            size = DpSize(initialBounds.width.dp, initialBounds.height.dp),
            position =
                initialBounds
                    .restorablePosition(currentScreenBounds())
                    ?.let { WindowPosition.Absolute(it.x.dp, it.y.dp) }
                    // 位置が未保存、または保存時とモニタ構成が変わって画面外になる場合は既定位置に出す。
                    ?: WindowPosition.Aligned(Alignment.TopCenter),
        )
    LaunchedEffect(windowState, onBoundsChange) {
        snapshotFlow { windowState.position to windowState.size }
            .mapNotNull { (position, size) -> (position as? WindowPosition.Absolute)?.let { it to size } }
            .map { (position, size) ->
                NarratorOverlayWindowGeometry(
                    x = position.x.value.roundToInt(),
                    y = position.y.value.roundToInt(),
                    width = size.width.value.roundToInt(),
                    height = size.height.value.roundToInt(),
                )
            }.distinctUntilChanged()
            .debounce(BOUNDS_SAVE_DEBOUNCE_MILLIS)
            .collect { geometry ->
                onBoundsChange(geometry.x, geometry.y, geometry.width, geometry.height)
            }
    }
    SwingWindow(
        onCloseRequest = {},
        state = windowState,
        title = "KoDriver Narrator Overlay",
        decoration = WindowDecoration.Undecorated(),
        transparent = true,
        resizable = true,
        alwaysOnTop = true,
        init = { composeWindow ->
            composeWindow.type = Window.Type.UTILITY
            composeWindow.minimumSize =
                Dimension(NARRATOR_OVERLAY_MIN_SIZE.width.value.toInt(), NARRATOR_OVERLAY_MIN_SIZE.height.value.toInt())
            composeWindow.maximumSize =
                Dimension(NARRATOR_OVERLAY_MAX_SIZE.width.value.toInt(), NARRATOR_OVERLAY_MAX_SIZE.height.value.toInt())
        },
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        var dragStartWindowLocation: Point? = null
                        var dragStartPointerScreenLocation: Point? = null
                        detectDragGestures(
                            onDragStart = {
                                dragStartWindowLocation = window.location
                                dragStartPointerScreenLocation = MouseInfo.getPointerInfo()?.location
                            },
                        ) { change, _ ->
                            change.consume()
                            val startWindowLocation = dragStartWindowLocation ?: return@detectDragGestures
                            val startPointerLocation = dragStartPointerScreenLocation ?: return@detectDragGestures
                            val currentPointerLocation =
                                MouseInfo.getPointerInfo()?.location ?: return@detectDragGestures
                            window.location =
                                Point(
                                    startWindowLocation.x + (currentPointerLocation.x - startPointerLocation.x),
                                    startWindowLocation.y + (currentPointerLocation.y - startPointerLocation.y),
                                )
                        }
                    },
        ) {
            NarratorOverlayScreen()
        }
    }
}

/**
 * オーバーレイ用ウィンドウを、ユーザー設定（その他タブの「オーバーレイ設定」→「オーバーレイを表示」）に
 * 応じて開閉し、位置・サイズの保存値を復元・永続化する。設定をOFFにするとウィンドウ自体を閉じるため、
 * ゲーム画面の操作を妨げなくなる。
 *
 * [rememberNarratorOverlayVisible] と [rememberNarratorOverlayBounds] は、設定の読み込みが完了するまで
 * `null` を返す。読み込み前にウィンドウを生成すると、設定がOFFのユーザーで起動直後に一瞬ウィンドウが
 * 開いて閉じたり、既定位置で開いてから保存位置へ飛んだりするため、どちらも揃ってから生成する。
 */
@Composable
fun ApplicationScope.NarratorOverlayWindowHost(modifier: Modifier = Modifier) {
    val visible = rememberNarratorOverlayVisible()
    val bounds = rememberNarratorOverlayBounds()
    val saveBounds = rememberNarratorOverlayBoundsSaver()
    if (visible == true && bounds != null) {
        NarratorOverlayWindow(
            initialBounds = bounds,
            onBoundsChange = saveBounds,
            modifier = modifier,
        )
    }
}
