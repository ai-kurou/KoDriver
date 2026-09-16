package kurou.kodriver

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kurou.kodriver.presentation.NarratorOverlayScreen
import java.awt.Dimension
import java.awt.Point
import kotlin.math.roundToInt

private val NARRATOR_OVERLAY_INITIAL_SIZE = DpSize(480.dp, 120.dp)
private val NARRATOR_OVERLAY_MIN_SIZE = DpSize(200.dp, 80.dp)
private val NARRATOR_OVERLAY_MAX_SIZE = DpSize(1200.dp, 600.dp)

/**
 * ゲーム画面に重ねて表示するオーバーレイ専用ウィンドウ（Windows版デスクトップアプリのみ、常時表示）。
 *
 * - `undecorated = true` かつ `resizable = true` の組み合わせで、Compose Multiplatform 独自の
 *   リサイズハンドル（[androidx.compose.ui.window.UndecoratedWindowResizer]）が有効になる。
 * - パネル全体のドラッグで移動できるよう、[detectDragGestures] でウィンドウ位置を直接更新する。
 * - タスクバーに表示させないため、ウィンドウがまだ非表示（displayable になる前）のうちに
 *   `Window.Type.UTILITY` を設定する。表示後に変更すると例外になるため、初期表示前の
 *   タイミング（[visible] = false の間）でのみ行う。
 * - 位置・サイズの永続化、表示ON/OFFの切り替え、常時最前面の詳細な制御（フォーカス連動等）は別PRで対応する。
 * - このウィンドウ自体は最前面には出ないため、LMU 側をボーダーレスウィンドウモードで起動する前提となる
 *   （排他的フルスクリーンでは他の常駐オーバーレイツールと同様に表示されない）。
 */
@Composable
fun ApplicationScope.NarratorOverlayWindow(modifier: Modifier = Modifier) {
    val windowState =
        rememberWindowState(
            size = NARRATOR_OVERLAY_INITIAL_SIZE,
            position = WindowPosition.Aligned(Alignment.TopCenter),
        )
    var visible by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = {},
        state = windowState,
        visible = visible,
        title = "KoDriver Narrator Overlay",
        undecorated = true,
        resizable = true,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(Unit) {
            window.type = java.awt.Window.Type.UTILITY
            window.minimumSize =
                Dimension(NARRATOR_OVERLAY_MIN_SIZE.width.value.toInt(), NARRATOR_OVERLAY_MIN_SIZE.height.value.toInt())
            window.maximumSize =
                Dimension(NARRATOR_OVERLAY_MAX_SIZE.width.value.toInt(), NARRATOR_OVERLAY_MAX_SIZE.height.value.toInt())
            visible = true
        }
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
