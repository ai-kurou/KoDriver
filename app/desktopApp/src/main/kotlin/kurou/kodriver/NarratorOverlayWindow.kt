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
import java.awt.Dimension
import java.awt.Point
import java.awt.Window
import kotlin.math.roundToInt

private val NARRATOR_OVERLAY_INITIAL_SIZE = DpSize(480.dp, 120.dp)
private val NARRATOR_OVERLAY_MIN_SIZE = DpSize(200.dp, 80.dp)
private val NARRATOR_OVERLAY_MAX_SIZE = DpSize(1200.dp, 600.dp)

/**
 * ゲーム画面に重ねて表示するオーバーレイ専用ウィンドウ（Windows版デスクトップアプリのみ、常時表示）。
 *
 * - `WindowDecoration.Undecorated()` かつ `resizable = true` の組み合わせで、Compose Multiplatform 独自の
 *   リサイズハンドル（[androidx.compose.ui.window.UndecoratedWindowResizer]）が有効になる。
 * - パネル全体のドラッグで移動できるよう、[detectDragGestures] でウィンドウ位置を直接更新する。
 * - タスクバーに表示させないため `Window.Type.UTILITY` を設定する。`java.awt.Window#setType` は
 *   ウィンドウが displayable になった後に呼ぶと `IllegalComponentStateException` を送出するため、
 *   通常の `Window`composable の content 内 `LaunchedEffect` から設定すると、環境によっては
 *   既に displayable になっていて例外が発生しうる（実機で確認済み）。[SwingWindow] の `init` は
 *   `ComposeWindow` 生成直後・displayable になる前に一度だけ呼ばれることが保証されているため、
 *   最小/最大サイズの設定とあわせてここで行う。
 * - 位置・サイズの永続化、表示ON/OFFの切り替え、常時最前面の詳細な制御（フォーカス連動等）は別PRで対応する。
 * - このウィンドウ自体は最前面には出ないため、LMU 側をボーダーレスウィンドウモードで起動する前提となる
 *   （排他的フルスクリーンでは他の常駐オーバーレイツールと同様に表示されない）。
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
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val location = window.location
                            window.location =
                                Point(
                                    location.x + dragAmount.x.roundToInt(),
                                    location.y + dragAmount.y.roundToInt(),
                                )
                        }
                    },
        ) {
            NarratorOverlayScreen()
        }
    }
}
