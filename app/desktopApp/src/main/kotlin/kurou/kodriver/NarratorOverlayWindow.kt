package kurou.kodriver

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.rememberWindowState
import kurou.kodriver.presentation.NarratorOverlayScreen
import kurou.kodriver.presentation.rememberNarratorOverlayVisible
import java.awt.Dimension
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window

private val NARRATOR_OVERLAY_INITIAL_SIZE = DpSize(480.dp, 120.dp)
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
 * - 位置・サイズの永続化、常時最前面の詳細な制御（フォーカス連動等）は別PRで対応する。
 * - このウィンドウ自体は最前面には出ないため、LMU 側をボーダーレスウィンドウモードで起動する前提となる
 *   （排他的フルスクリーンでは他の常駐オーバーレイツールと同様に表示されない）。
 * - ゲーム画面をなるべく隠さないよう `transparent = true` でウィンドウ背景を透過させ、コンテンツ側
 *   （[NarratorOverlayScreen]）の半透明な背景色と組み合わせている。`isTransparent`（`transparent`
 *   パラメータの実体）はウィンドウが表示済みの状態で変更すると例外を送出するが、`SwingWindow` の
 *   `update` ブロックは値に変化がない限り再設定を行わないため、固定値 `true` を渡す限りは
 *   ウィンドウがまだ displayable になる前の初回 `update` 呼び出し時にのみ設定される。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScope.NarratorOverlayWindow(modifier: Modifier = Modifier) {
    val windowState =
        rememberWindowState(
            size = NARRATOR_OVERLAY_INITIAL_SIZE,
            position = WindowPosition.Aligned(Alignment.TopCenter),
        )
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
 * 応じて開閉する。設定をOFFにするとウィンドウ自体を閉じるため、ゲーム画面の操作を妨げなくなる。
 *
 * [rememberNarratorOverlayVisible] は設定の読み込みが完了するまで `null` を返す。読み込み前に
 * ウィンドウを生成すると、設定がOFFのユーザーで起動直後に一瞬ウィンドウが開いて閉じるため、
 * `true` のときだけ生成する。
 */
@Composable
fun ApplicationScope.NarratorOverlayWindowHost(modifier: Modifier = Modifier) {
    if (rememberNarratorOverlayVisible() == true) {
        NarratorOverlayWindow(modifier = modifier)
    }
}
