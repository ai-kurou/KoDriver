package kurou.kodriver.core.designsystem

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalInputModeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 一覧の行ごとの「主操作（詳細を開く領域）」の [FocusRequester] と、現在フォーカスのある行を保持する。
 *
 * 行内のスイッチ・トグルなどにフォーカスがあっても、↑↓ では隣の行の主操作領域へ移動する（行単位の移動）。
 * 並べ替えで行の位置が変わっても追従できるよう、フォーカス行はインデックスではなく項目キーで記録する。
 */
public class ListRowFocusState {
    private val requesters = mutableMapOf<String, FocusRequester>()

    /** フォーカスのある行の項目キー。行外なら null。 */
    public var focusedItemKey: String? = null

    /** [itemKey] の行の主操作領域に対応する [FocusRequester]。未生成なら作成して返す。 */
    public fun requesterFor(itemKey: String): FocusRequester = requesters.getOrPut(itemKey) { FocusRequester() }

    /** 行にフォーカスが入ったら [itemKey] を記録し、外れたときは自分の行の記録だけを消す。 */
    public fun onRowFocusChanged(
        itemKey: String,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            focusedItemKey = itemKey
        } else if (focusedItemKey == itemKey) {
            focusedItemKey = null
        }
    }

    private var lastReorderRequestedFrom: List<String>? = null

    /**
     * Alt+↑↓ の並べ替えを依頼してよいか返す。
     *
     * 直前に依頼したときと同じ並び [itemKeys] のままなら、その並べ替えがまだ一覧に反映されていない
     * （キーリピートが再描画より先に届いた）ため false を返す。古い並びのインデックスで重ねて依頼すると、
     * 別の行が動いたり移動が失われたりするため、反映されるまでは依頼しない。依頼してよい場合は並びを記録して true を返す。
     */
    public fun tryStartReorder(itemKeys: List<String>): Boolean {
        val canStart = lastReorderRequestedFrom != itemKeys
        if (canStart) {
            lastReorderRequestedFrom = itemKeys
        }
        return canStart
    }

    /** 一覧の並びが変わったら呼ぶ。保留中の並べ替えは反映済みとみなし、ドラッグ等で元の並びに戻っても再び依頼できるようにする。 */
    public fun onItemKeysChanged() {
        lastReorderRequestedFrom = null
    }
}

/** ↑↓ キーを受けたときの処理方針。 */
internal sealed interface ListRowArrowKeyAction {
    /** 処理せず、既定のフォーカス移動に任せる。 */
    data object Ignore : ListRowArrowKeyAction

    /** 端でそれ以上動けない。何もせずキーだけ消費する。 */
    data object Stay : ListRowArrowKeyAction

    /** [itemIndex] 番目の項目の行へフォーカスを移す。 */
    data class MoveTo(
        val itemIndex: Int,
    ) : ListRowArrowKeyAction
}

/**
 * ↑ なら -1、↓ なら +1 を返す。それ以外のキー・KeyUp・修飾キー付き（Alt+↑↓ 等）の場合は null。
 */
internal fun listRowArrowKeyStep(event: KeyEvent): Int? {
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
 * - 対象外のキー、またはフォーカスが行の外（ヒント行など）にある場合は [ListRowArrowKeyAction.Ignore]。
 * - 先頭で ↑、末尾で ↓ のときは [ListRowArrowKeyAction.Stay]（折り返さず、一覧の外にも出ない）。
 */
internal fun listRowArrowKeyAction(
    step: Int?,
    focusedItemKey: String?,
    itemKeys: List<String>,
): ListRowArrowKeyAction {
    val current = itemKeys.indexOfFirst { it == focusedItemKey }
    return when {
        step == null || current < 0 -> ListRowArrowKeyAction.Ignore
        (current + step) in itemKeys.indices -> ListRowArrowKeyAction.MoveTo(current + step)
        else -> ListRowArrowKeyAction.Stay
    }
}

/**
 * 一覧の ↑↓ キーを行単位の移動として処理し、処理した（消費すべき）場合は true を返す。
 * 移動先が画面外で未生成の場合は、スクロールしてからフォーカスを移す。
 *
 * [itemKeys] は行の並び順に並んだ項目キー、[firstItemLazyIndex] は先頭項目の LazyList 上のインデックス。
 * 項目が連続しない LazyColumn（見出しや区切り行を挟む場合）は、[lazyIndexOf] で項目の並び順のインデックスから
 * LazyList 上のインデックスを求める。既定値は [firstItemLazyIndex] から連続していると見なす。
 */
public fun handleListRowArrowKey(
    event: KeyEvent,
    focusState: ListRowFocusState,
    itemKeys: List<String>,
    firstItemLazyIndex: Int,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    lazyIndexOf: (Int) -> Int = { firstItemLazyIndex + it },
): Boolean {
    val action = listRowArrowKeyAction(listRowArrowKeyStep(event), focusState.focusedItemKey, itemKeys)
    if (action is ListRowArrowKeyAction.MoveTo) {
        val requester = focusState.requesterFor(itemKeys[action.itemIndex])
        coroutineScope.launch { listState.focusRow(lazyIndexOf(action.itemIndex), requester) }
    }
    return action != ListRowArrowKeyAction.Ignore
}

/**
 * Alt（Mac では Option）+ ↑ なら -1、Alt + ↓ なら +1 を返す。
 * Alt 以外の修飾キーを併用した場合、KeyUp、他のキーは null。
 */
internal fun listRowReorderStep(event: KeyEvent): Int? {
    val altOnly = event.isAltPressed && !event.isCtrlPressed && !event.isMetaPressed && !event.isShiftPressed
    val step =
        when {
            event.type != KeyEventType.KeyDown || !altOnly -> null
            event.key == Key.DirectionUp -> -1
            event.key == Key.DirectionDown -> 1
            else -> null
        }
    return step
}

/**
 * 一覧の Alt（Mac では Option）+ ↑↓ を、フォーカス行の並べ替えとして処理し、処理した（消費すべき）場合は true を返す。
 *
 * 移動可能なら [onMove] に（移動元, 移動先）の項目インデックスを渡し、移動後も同じ行にフォーカスが残るよう
 * 主操作領域へフォーカスを戻す（画面外になる場合はスクロールする）。先頭で ↑、末尾で ↓ のときは何も動かさず消費する。
 * フォーカスが行の外にある場合は処理しない。引数の意味は [handleListRowArrowKey] と同じ。
 */
public fun handleListRowReorderKey(
    event: KeyEvent,
    focusState: ListRowFocusState,
    itemKeys: List<String>,
    firstItemLazyIndex: Int,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    onMove: (from: Int, to: Int) -> Unit,
): Boolean {
    val action = listRowArrowKeyAction(listRowReorderStep(event), focusState.focusedItemKey, itemKeys)
    if (action is ListRowArrowKeyAction.MoveTo && focusState.tryStartReorder(itemKeys)) {
        val from = itemKeys.indexOfFirst { it == focusState.focusedItemKey }
        onMove(from, action.itemIndex)
        val requester = focusState.requesterFor(itemKeys[from])
        coroutineScope.launch { listState.focusRow(firstItemLazyIndex + action.itemIndex, requester) }
    }
    return action != ListRowArrowKeyAction.Ignore
}

/**
 * LazyList を使わない縦並びの一覧（[itemKeys] の並び順）で、[index] 番目の行にフォーカスがあるときの Alt（Mac では Option）+ ↑↓ を
 * 並べ替えとして処理し、処理した（消費すべき）場合は true を返す。
 *
 * 移動可能なら [onMove] に（移動元, 移動先）のインデックスを渡す。先頭で ↑、末尾で ↓ のときは何も動かさず消費する。
 * Alt 以外の修飾キー付きや他のキーは処理しない。並べ替えが一覧に反映される前に届いたキーリピートは、
 * 古いインデックスで別の行を動かさないよう [focusState] の [ListRowFocusState.tryStartReorder] で抑止する
 * （並びが変わったら [ListRowFocusState.onItemKeysChanged] を呼ぶこと）。
 */
public fun handleColumnRowReorderKey(
    event: KeyEvent,
    index: Int,
    itemKeys: List<String>,
    focusState: ListRowFocusState,
    onMove: (from: Int, to: Int) -> Unit,
): Boolean {
    val step = listRowReorderStep(event)
    if (step != null && (index + step) in itemKeys.indices && focusState.tryStartReorder(itemKeys)) {
        onMove(index, index + step)
    }
    return step != null
}

/**
 * 詳細から一覧へ戻ったとき、直前に開いた行 [restoreItemKey] の主操作領域へフォーカスを戻す。
 *
 * [restoreItemKey] が null の間は何もしない。null でなくなったらキーボード操作のときだけフォーカスを要求し、
 * 要求の成否にかかわらず [onRestored] を呼ぶ（呼び出し側で復元キーを消し、タブ切り替えなどの再表示で再び奪わないようにする）。
 * 行が画面外で未生成のときは、[listState] と [lazyIndexOf]（項目キーから LazyList 上のインデックス）を渡すとスクロールしてから移す。
 * 一覧が詳細と並んで表示されている間はフォーカスを奪わないよう、戻った（選択が解除された）ときだけ [restoreItemKey] を渡すこと。
 */
@Composable
public fun RestoreRowFocusOnReturn(
    restoreItemKey: String?,
    focusState: ListRowFocusState,
    onRestored: () -> Unit,
    listState: LazyListState? = null,
    lazyIndexOf: (String) -> Int? = { null },
) {
    val inputModeManager = LocalInputModeManager.current
    val currentOnRestored by rememberUpdatedState(onRestored)
    val currentLazyIndexOf by rememberUpdatedState(lazyIndexOf)
    LaunchedEffect(restoreItemKey) {
        if (restoreItemKey != null) {
            if (inputModeManager.inputMode == InputMode.Keyboard) {
                val requester = focusState.requesterFor(restoreItemKey)
                val lazyIndex = currentLazyIndexOf(restoreItemKey)
                if (listState != null && lazyIndex != null) {
                    listState.focusRow(lazyIndex, requester)
                } else {
                    withFrameNanos { }
                    runCatching { requester.requestFocus() }
                }
            }
            currentOnRestored()
        }
    }
}

/**
 * タブの切り替えなどで一覧が表示されたとき、先頭の行 [firstItemKey] の主操作領域へ一度だけフォーカスを置く。
 *
 * [enabled] が false で表示された場合は、その表示では一切フォーカスを置かない
 * （詳細が選択済みで詳細側が初期フォーカスを持つ場合や、詳細から戻って [RestoreRowFocusOnReturn] が直前の行へ戻す場合）。
 * 先頭の行がまだ無い（[firstItemKey] が null）間は待ち、決まった時点で一度だけ要求する。
 * キーボード操作のときだけフォーカスを要求し、行が画面外のときのスクロールは [RestoreRowFocusOnReturn] と同じ。
 */
@Composable
public fun FocusFirstRowOnEntry(
    firstItemKey: String?,
    enabled: Boolean,
    focusState: ListRowFocusState,
    listState: LazyListState? = null,
    lazyIndexOf: (String) -> Int? = { null },
) {
    var pending by remember { mutableStateOf(enabled) }
    LaunchedEffect(enabled) {
        if (!enabled) {
            pending = false
        }
    }
    RestoreRowFocusOnReturn(
        restoreItemKey = firstItemKey.takeIf { pending && enabled },
        focusState = focusState,
        onRestored = { pending = false },
        listState = listState,
        lazyIndexOf = lazyIndexOf,
    )
}

private suspend fun LazyListState.focusRow(
    lazyIndex: Int,
    requester: FocusRequester,
) = focusItemWhenComposed(
    isVisible = layoutInfo.visibleItemsInfo.any { it.index == lazyIndex },
    scrollToItem = { scrollToItem(lazyIndex) },
    requester = requester,
)

/**
 * 移動先が画面外で未生成なら [scrollToItem] で表示してから、[requester] へフォーカスを移す。
 * 紐付いていない場合は IllegalStateException になるため、失敗は無視してフォーカスを動かさない。
 */
internal suspend fun focusItemWhenComposed(
    isVisible: Boolean,
    scrollToItem: suspend () -> Unit,
    requester: FocusRequester,
) {
    if (!isVisible) {
        scrollToItem()
        // スクロール先の行が合成され、FocusRequester が紐付くのを待つ
        withFrameNanos { }
    }
    runCatching { requester.requestFocus() }
}
