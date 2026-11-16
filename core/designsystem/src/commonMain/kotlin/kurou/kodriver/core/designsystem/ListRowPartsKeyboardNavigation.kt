package kurou.kodriver.core.designsystem

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.KeyEvent

/**
 * 一覧の1行の中にある操作部品（主操作領域・スイッチ・トグルなど）の [FocusRequester] と、フォーカス中の部品を保持する。
 *
 * 部品は行内の並び順（左→右、上→下）に 0 から番号を振る。0 番は行の主操作領域で、[firstRequester] を使う。
 * 行の ↑↓ 移動（[ListRowFocusState]）とは独立していて、←→ で同じ行の部品間を移動するために使う。
 */
public class ListRowPartsFocusState(
    public val partCount: Int,
    firstRequester: FocusRequester,
) {
    private val requesters = listOf(firstRequester) + List(partCount - 1) { FocusRequester() }

    /** フォーカス中の部品の番号。行内の部品にフォーカスがなければ null。 */
    public var focusedPart: Int? = null
        private set

    /** [part] 番目の部品に対応する [FocusRequester]。 */
    public fun requesterFor(part: Int): FocusRequester = requesters[part]

    /** 部品にフォーカスが入ったら [part] を記録し、外れたときは自分の記録だけを消す。 */
    public fun onPartFocusChanged(
        part: Int,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            focusedPart = part
        } else if (focusedPart == part) {
            focusedPart = null
        }
    }
}

/** ← なら -1、→ なら +1 を返す。それ以外のキー・KeyUp・修飾キー付きの場合は null。 */
internal fun listRowPartArrowKeyStep(event: KeyEvent): Int? =
    when (listGridArrowKeyDirection(event)) {
        ListGridDirection.Left -> -1
        ListGridDirection.Right -> 1
        else -> null
    }

/**
 * [step]（← -1 / → +1。対象外のキーは null）と現在の部品 [focusedPart] から、処理方針を決める。
 *
 * [partEnabled] は部品ごとにフォーカスできるか（無効な部品は飛ばす）を表す。
 * - 対象外のキー、またはフォーカスが行内の部品にない場合は [ListRowArrowKeyAction.Ignore]。
 * - 端で、それ以上フォーカスできる部品がない場合は [ListRowArrowKeyAction.Stay]（折り返さず、行の外にも出ない）。
 */
internal fun listRowPartArrowKeyAction(
    step: Int?,
    focusedPart: Int?,
    partEnabled: List<Boolean>,
): ListRowArrowKeyAction {
    val action =
        if (step == null || focusedPart == null || focusedPart !in partEnabled.indices) {
            ListRowArrowKeyAction.Ignore
        } else {
            generateSequence(focusedPart + step) { it + step }
                .takeWhile { it in partEnabled.indices }
                .firstOrNull { partEnabled[it] }
                ?.let { ListRowArrowKeyAction.MoveTo(it) }
                ?: ListRowArrowKeyAction.Stay
        }
    return action
}

/**
 * 行内の ←→ キーを、同じ行の部品間の移動として処理し、処理した（消費すべき）場合は true を返す。
 *
 * [partEnabled] の要素数は [ListRowPartsFocusState.partCount] と同じにする。
 * 行の ↑↓ 移動（[handleListRowArrowKey]）と同様に、行外の要素にフォーカスがあるときは処理しない。
 */
public fun handleListRowPartArrowKey(
    event: KeyEvent,
    partsState: ListRowPartsFocusState,
    partEnabled: List<Boolean>,
): Boolean {
    val action = listRowPartArrowKeyAction(listRowPartArrowKeyStep(event), partsState.focusedPart, partEnabled)
    if (action is ListRowArrowKeyAction.MoveTo) {
        runCatching { partsState.requesterFor(action.itemIndex).requestFocus() }
    }
    return action != ListRowArrowKeyAction.Ignore
}
