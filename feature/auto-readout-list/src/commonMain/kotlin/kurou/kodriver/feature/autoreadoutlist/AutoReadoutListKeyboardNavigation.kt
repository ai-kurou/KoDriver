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

/**
 * 一覧の行ごとの「主操作（詳細を開く領域）」の [FocusRequester] と、現在フォーカスのある行を保持する。
 *
 * 行内のスイッチ・トグルなどにフォーカスがあっても、↑↓ では隣の行の主操作領域へ移動する（行単位の移動）。
 * 並べ替えで行の位置が変わっても追従できるよう、フォーカス行はインデックスではなく項目キーで記録する。
 */
internal class AutoReadoutRowFocusState {
    private val requesters = mutableMapOf<String, FocusRequester>()

    /** フォーカスのある行の項目キー（[ReadoutItemKey.value]）。行外なら null。 */
    var focusedItemKey: String? = null

    fun requesterFor(item: ReadoutItemKey): FocusRequester = requesters.getOrPut(item.value) { FocusRequester() }

    /** 行にフォーカスが入ったら [itemKey] を記録し、外れたときは自分の行の記録だけを消す。 */
    fun onRowFocusChanged(
        itemKey: String,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            focusedItemKey = itemKey
        } else if (focusedItemKey == itemKey) {
            focusedItemKey = null
        }
    }
}

/** ↑↓ キーを受けたときの処理方針。 */
internal sealed interface AutoReadoutArrowKeyAction {
    /** 処理せず、既定のフォーカス移動に任せる。 */
    data object Ignore : AutoReadoutArrowKeyAction

    /** 端でそれ以上動けない。何もせずキーだけ消費する。 */
    data object Stay : AutoReadoutArrowKeyAction

    /** [itemIndex] 番目の項目の行へフォーカスを移す。 */
    data class MoveTo(
        val itemIndex: Int,
    ) : AutoReadoutArrowKeyAction
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

/**
 * [step]（↑ -1 / ↓ +1。対象外のキーは null）と現在のフォーカス行 [focusedItemKey] から、処理方針を決める。
 *
 * - 対象外のキー、またはフォーカスが行の外（ヒント行など）にある場合は [AutoReadoutArrowKeyAction.Ignore]。
 * - 先頭で ↑、末尾で ↓ のときは [AutoReadoutArrowKeyAction.Stay]（折り返さず、一覧の外にも出ない）。
 */
internal fun autoReadoutArrowKeyAction(
    step: Int?,
    focusedItemKey: String?,
    items: List<ReadoutItemKey>,
): AutoReadoutArrowKeyAction {
    val current = items.indexOfFirst { it.value == focusedItemKey }
    return when {
        step == null || current < 0 -> AutoReadoutArrowKeyAction.Ignore
        (current + step) in items.indices -> AutoReadoutArrowKeyAction.MoveTo(current + step)
        else -> AutoReadoutArrowKeyAction.Stay
    }
}

/**
 * 一覧の ↑↓ キーを行単位の移動として処理し、処理した（消費すべき）場合は true を返す。
 * 移動先が画面外で未生成の場合は、スクロールしてからフォーカスを移す。
 */
internal fun handleAutoReadoutArrowKey(
    event: KeyEvent,
    focusState: AutoReadoutRowFocusState,
    items: List<ReadoutItemKey>,
    firstItemLazyIndex: Int,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
): Boolean {
    val action = autoReadoutArrowKeyAction(autoReadoutArrowKeyStep(event), focusState.focusedItemKey, items)
    if (action is AutoReadoutArrowKeyAction.MoveTo) {
        val requester = focusState.requesterFor(items[action.itemIndex])
        coroutineScope.launch { listState.focusRow(firstItemLazyIndex + action.itemIndex, requester) }
    }
    return action != AutoReadoutArrowKeyAction.Ignore
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
