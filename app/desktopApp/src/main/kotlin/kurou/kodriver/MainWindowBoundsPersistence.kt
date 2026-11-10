package kurou.kodriver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kurou.kodriver.presentation.MainWindowSavedBounds
import java.awt.Rectangle
import kotlin.math.roundToInt

/** メインウィンドウの最小サイズ。保存値がこれを下回っていても、この値まで引き上げて復元する。 */
internal val MAIN_WINDOW_MIN_SIZE = DpSize(600.dp, 500.dp)

/** 位置の復元可否を判定する、ウィンドウ上端の帯の高さ（タイトルバーを含む）。 */
private const val TITLE_BAR_STRIP_HEIGHT = 40

/** ドラッグ・リサイズ中は座標が連続で変化するため、操作が落ち着いてから一度だけ保存するまでの待ち時間。 */
private const val BOUNDS_SAVE_DEBOUNCE_MILLIS = 500L

/**
 * 保存値から復元するメインウィンドウの状態。[position] が `null` の場合は位置を変えず、OS 任せの既定位置のままにする。
 */
internal data class RestoredMainWindowBounds(
    val size: DpSize,
    val position: WindowPosition.Absolute?,
)

/**
 * 保存値をメインウィンドウへ復元する値に変換する。
 *
 * - サイズは [MAIN_WINDOW_MIN_SIZE] を下回らないようにする（最小サイズ未満だとウィンドウが縮められない）。
 * - 位置は現在のモニタ構成で画面内に収まっている場合だけ復元する。モニタを外した・解像度を変えた等で
 *   画面外になる場合は `null` とし、既定位置のまま開く（タイトルバーが画面外で掴めなくなるのを防ぐ）。
 */
internal fun restoreMainWindowBounds(
    saved: MainWindowSavedBounds,
    screenBounds: List<Rectangle>,
): RestoredMainWindowBounds {
    val width = saved.width.coerceAtLeast(MAIN_WINDOW_MIN_SIZE.width.value.toInt())
    val height = saved.height.coerceAtLeast(MAIN_WINDOW_MIN_SIZE.height.value.toInt())
    // タイトルバーを含む上端の帯だけで判定する。下端だけが画面内に残るとタイトルバーが掴めなくなるため。
    val point = restorablePosition(saved.x, saved.y, width, TITLE_BAR_STRIP_HEIGHT, screenBounds)
    return RestoredMainWindowBounds(
        size = DpSize(width.dp, height.dp),
        position = point?.let { WindowPosition.Absolute(it.x.dp, it.y.dp) },
    )
}

/**
 * メインウィンドウの位置・サイズを、前回終了時の保存値へ復元し、以降の移動・リサイズを保存する。
 *
 * - [savedBounds] が `null`（読み込み中）の間は何もしない。最初に読み込めた時点で一度だけ復元する。
 *   復元前に保存を始めると、既定値で保存値を上書きしてしまうため、保存は復元の後に開始する。
 * - 最大化・最小化・全画面の間は保存しない。その状態のサイズや位置を保存すると、次回起動時に画面いっぱいの
 *   大きさで復元されたり、画面外の座標が保存されたりする。通常のウィンドウに戻った時点の値が保存される。
 * - 保存値の購読元（Koin）はウィンドウより後に初期化されるため、ウィンドウは既定の大きさで先に開き、
 *   読み込み完了後に復元する。
 */
@OptIn(FlowPreview::class)
@Composable
internal fun MainWindowBoundsPersistence(
    windowState: WindowState,
    savedBounds: MainWindowSavedBounds?,
    onBoundsChange: (x: Int, y: Int, width: Int, height: Int) -> Unit,
    screenBounds: () -> List<Rectangle> = ::currentScreenBounds,
) {
    var restored by remember { mutableStateOf(false) }
    LaunchedEffect(savedBounds != null) {
        val saved = savedBounds ?: return@LaunchedEffect
        val bounds = restoreMainWindowBounds(saved, screenBounds())
        windowState.size = bounds.size
        bounds.position?.let { windowState.position = it }
        restored = true
    }
    LaunchedEffect(restored, windowState, onBoundsChange) {
        if (!restored) return@LaunchedEffect
        snapshotFlow {
            val position = windowState.position as? WindowPosition.Absolute
            if (windowState.placement == WindowPlacement.Floating && !windowState.isMinimized && position != null) {
                listOf(
                    position.x.value.roundToInt(),
                    position.y.value.roundToInt(),
                    windowState.size.width.value
                        .roundToInt(),
                    windowState.size.height.value
                        .roundToInt(),
                )
            } else {
                null
            }
        }.distinctUntilChanged()
            .debounce(BOUNDS_SAVE_DEBOUNCE_MILLIS)
            .filterNotNull()
            .collect { (x, y, width, height) -> onBoundsChange(x, y, width, height) }
    }
}
