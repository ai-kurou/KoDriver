package kurou.kodriver.core.designsystem

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** グリッドで矢印キーを受けたときの移動方向。 */
enum class ListGridDirection { Up, Down, Left, Right }

/**
 * ↑↓←→ に対応する [ListGridDirection] を返す。
 * それ以外のキー・KeyUp・修飾キー付き（Alt+矢印 等）の場合は null。
 */
fun listGridArrowKeyDirection(event: KeyEvent): ListGridDirection? {
    val hasModifier = event.isAltPressed || event.isCtrlPressed || event.isMetaPressed || event.isShiftPressed
    val direction =
        when {
            event.type != KeyEventType.KeyDown || hasModifier -> null
            event.key == Key.DirectionUp -> ListGridDirection.Up
            event.key == Key.DirectionDown -> ListGridDirection.Down
            event.key == Key.DirectionLeft -> ListGridDirection.Left
            event.key == Key.DirectionRight -> ListGridDirection.Right
            else -> null
        }
    return direction
}

/**
 * [columns] 列のグリッドで、[direction] と現在のフォーカス項目 [focusedItemKey] から処理方針を決める。
 * 項目は左上から右へ、行ごとに並んでいるものとする。
 *
 * - 対象外のキー、またはフォーカスが項目の外にある場合は [ListRowArrowKeyAction.Ignore]。
 * - ←→ は同じ行の隣へ移動し、行頭の ← と行末（最後の項目を含む）の → は [ListRowArrowKeyAction.Stay]。
 * - ↑↓ は同じ列の上下へ移動し、先頭行の ↑ と最終行の ↓ は [ListRowArrowKeyAction.Stay]。
 *   最終行が欠けていて真下の項目がない場合は、最後の項目へ移動する。
 * - いずれも折り返さず、グリッドの外にも出ない。
 */
fun listGridArrowKeyAction(
    direction: ListGridDirection?,
    focusedItemKey: String?,
    itemKeys: List<String>,
    columns: Int,
): ListRowArrowKeyAction {
    val current = itemKeys.indexOfFirst { it == focusedItemKey }
    return if (direction == null || current < 0) {
        ListRowArrowKeyAction.Ignore
    } else {
        listGridTargetIndex(direction, current, itemKeys.lastIndex, columns)
            ?.let { ListRowArrowKeyAction.MoveTo(it) }
            ?: ListRowArrowKeyAction.Stay
    }
}

/** [current] から [direction] へ動いた先のインデックス。動けない場合は null。[last] は最後の項目のインデックス。 */
private fun listGridTargetIndex(
    direction: ListGridDirection,
    current: Int,
    last: Int,
    columns: Int,
): Int? =
    when (direction) {
        ListGridDirection.Left -> (current - 1).takeIf { current % columns != 0 }
        ListGridDirection.Right -> (current + 1).takeIf { current % columns != columns - 1 && current < last }
        ListGridDirection.Up -> (current - columns).takeIf { it >= 0 }
        ListGridDirection.Down -> listGridDownIndex(current, last, columns)
    }

/** ↓ の移動先。真下がなくても下の行があれば最後の項目、最終行なら null。 */
private fun listGridDownIndex(
    current: Int,
    last: Int,
    columns: Int,
): Int? =
    when {
        current + columns <= last -> current + columns
        current / columns < last / columns -> last
        else -> null
    }

/**
 * グリッドの矢印キーを項目単位の移動として処理し、処理した（消費すべき）場合は true を返す。
 * 移動先が画面外で未生成の場合は、スクロールしてからフォーカスを移す。
 *
 * [itemKeys] は項目の並び順に並んだ項目キー、[firstItemLazyIndex] は先頭項目の LazyGrid 上のインデックス。
 * 項目はそこから連続していると見なす。[focusState] の扱いは [handleListRowArrowKey] と同じ。
 */
fun handleListGridArrowKey(
    event: KeyEvent,
    focusState: ListRowFocusState,
    itemKeys: List<String>,
    columns: Int,
    firstItemLazyIndex: Int,
    gridState: LazyGridState,
    coroutineScope: CoroutineScope,
): Boolean {
    val direction = listGridArrowKeyDirection(event)
    val action = listGridArrowKeyAction(direction, focusState.focusedItemKey, itemKeys, columns)
    if (action is ListRowArrowKeyAction.MoveTo) {
        val requester = focusState.requesterFor(itemKeys[action.itemIndex])
        val lazyIndex = firstItemLazyIndex + action.itemIndex
        coroutineScope.launch {
            focusItemWhenComposed(
                isVisible = gridState.layoutInfo.visibleItemsInfo.any { it.index == lazyIndex },
                scrollToItem = { gridState.scrollToItem(lazyIndex) },
                requester = requester,
            )
        }
    }
    return action != ListRowArrowKeyAction.Ignore
}
