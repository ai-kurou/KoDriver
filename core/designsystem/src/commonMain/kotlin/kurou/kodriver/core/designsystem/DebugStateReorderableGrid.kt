package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

/** デバッグ状態画面のカードグリッドに付けるテストタグ。 */
const val DEBUG_STATE_GRID_TEST_TAG = "debug_state_grid"

private val NARROW_WIDTH_UPPER_BOUND = 400.dp
private val MEDIUM_WIDTH_UPPER_BOUND = 700.dp

/** 幅が 400dp 未満なら1列、700dp 未満なら2列、それ以上なら3列にする。 */
fun calculateDebugStateColumns(maxWidth: Dp): Int =
    when {
        maxWidth < NARROW_WIDTH_UPPER_BOUND -> 1
        maxWidth < MEDIUM_WIDTH_UPPER_BOUND -> 2
        else -> 3
    }

/**
 * デバッグ状態画面の、長押しで並べ替えられるカードグリッド。
 *
 * 先頭に並べる固定項目（デバッグ読み上げ依頼カードなど）は [fixedItems] で受け取り、並べ替えの対象にしない。
 * カードは [cardOrder] の順に並び、[card] には余白と長押しドラッグを適用済みの modifier が渡る。
 * [onMoveCard] には固定項目を除いた、[cardOrder] 上の移動元・移動先のインデックスを通知する。
 *
 * @param cardOrder 表示するカードキーの並び。キーの名前を各項目の識別子にする
 * @param fixedItemCount [fixedItems] が追加する項目の数
 * @param fixedItems 先頭に固定で追加する項目
 * @param card キーに対応するカードを描画する
 */
@Composable
fun <K : Enum<K>> DebugStateReorderableGrid(
    cardOrder: List<K>,
    fixedItemCount: Int,
    onMoveCard: (from: Int, to: Int) -> Unit,
    fixedItems: LazyGridScope.() -> Unit,
    card: @Composable (cardKey: K, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val gridState = rememberLazyGridState()
        val reorderableState =
            rememberReorderableLazyGridState(gridState) { from, to ->
                if (from.index >= fixedItemCount && to.index >= fixedItemCount) {
                    onMoveCard(from.index - fixedItemCount, to.index - fixedItemCount)
                }
            }
        LazyVerticalGrid(
            columns = GridCells.Fixed(calculateDebugStateColumns(maxWidth)),
            state = gridState,
            modifier = Modifier.testTag(DEBUG_STATE_GRID_TEST_TAG),
        ) {
            fixedItems()
            items(cardOrder, key = { it.name }) { cardKey ->
                ReorderableItem(reorderableState, key = cardKey.name) {
                    card(cardKey, Modifier.padding(KoDriverSpacing.small).longPressDraggableHandle())
                }
            }
        }
    }
}
