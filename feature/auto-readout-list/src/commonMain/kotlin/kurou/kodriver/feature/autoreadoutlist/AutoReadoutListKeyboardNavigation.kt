package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
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
import kurou.kodriver.domain.model.ReadoutItemKey

/** 一覧内でフォーカスが行のどこにも無い状態を表す。 */
private const val NO_FOCUSED_ITEM = -1

/**
 * 一覧の行ごとの「主操作（詳細を開く領域）」の [FocusRequester] と、現在フォーカスのある行を保持する。
 *
 * 行内のスイッチ・トグルなどにフォーカスがあっても、↑↓ では隣の行の主操作領域へ移動する（行単位の移動）。
 */
internal class AutoReadoutRowFocusState {
    private val requesters = mutableMapOf<String, FocusRequester>()

    /** フォーカスのある行の項目インデックス（行外なら [NO_FOCUSED_ITEM]）。 */
    var focusedItemIndex: Int = NO_FOCUSED_ITEM

    fun requesterFor(item: ReadoutItemKey): FocusRequester = requesters.getOrPut(item.value) { FocusRequester() }

    /** 行にフォーカスが入ったら [index] を記録し、外れたときは自分の行の記録だけを消す。 */
    fun onRowFocusChanged(
        index: Int,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            focusedItemIndex = index
        } else if (focusedItemIndex == index) {
            focusedItemIndex = NO_FOCUSED_ITEM
        }
    }
}

/**
 * ↑ なら -1、↓ なら +1 を返す。それ以外のキー・KeyUp・修飾キー付き（Alt+↑↓ 等）の場合は null。
 */
internal fun autoReadoutArrowKeyStep(event: KeyEvent): Int? {
    val hasModifier = event.isAltPressed || event.isCtrlPressed || event.isMetaPressed || event.isShiftPressed
    val step =
        when {
            event.type != KeyEventType.KeyDown || hasModifier -> null
            event.key == Key.DirectionUp -> -1
            event.key == Key.DirectionDown -> 1
            else -> null
        }
    return step
}

/** [current] から [step] 移動した先の項目インデックス。範囲外（端を越える）なら null（折り返さない）。 */
internal fun adjacentAutoReadoutItemIndex(
    current: Int,
    step: Int,
    itemCount: Int,
): Int? = (current + step).takeIf { it in 0 until itemCount }

/**
 * 一覧の ↑↓ キーを行単位の移動として処理し、処理した（消費すべき）場合は true を返す。
 *
 * - フォーカスが行の外（ヒント行など）にある場合は何もせず false を返し、既定のフォーカス移動に任せる。
 * - 先頭で ↑、末尾で ↓ のときは何もしない（折り返さず、一覧の外にも出ない）が、キーは消費する。
 * - 移動先が画面外で未生成の場合は、スクロールしてからフォーカスを移す。
 */
internal fun handleAutoReadoutArrowKey(
    event: KeyEvent,
    focusState: AutoReadoutRowFocusState,
    items: List<ReadoutItemKey>,
    firstItemLazyIndex: Int,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
): Boolean {
    val step = autoReadoutArrowKeyStep(event)
    val current = focusState.focusedItemIndex
    if (step == null || current !in items.indices) return false
    val target = adjacentAutoReadoutItemIndex(current, step, items.size)
    if (target != null) {
        val requester = focusState.requesterFor(items[target])
        coroutineScope.launch { listState.focusRow(firstItemLazyIndex + target, requester) }
    }
    return true
}

private suspend fun LazyListState.focusRow(
    lazyIndex: Int,
    requester: FocusRequester,
) {
    if (layoutInfo.visibleItemsInfo.none { it.index == lazyIndex }) {
        scrollToItem(lazyIndex)
        // スクロール先の行が合成され、FocusRequester が紐付くのを待つ
        withFrameNanos { }
    }
    // 紐付いていない場合は IllegalStateException になるため、失敗は無視してフォーカスを動かさない
    runCatching { requester.requestFocus() }
}
