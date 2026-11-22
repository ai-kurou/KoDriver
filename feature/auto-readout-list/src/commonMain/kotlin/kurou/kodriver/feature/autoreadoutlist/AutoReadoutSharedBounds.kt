package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 一覧の行と詳細の TopAppBar を共有要素として結び、一覧から詳細へ遷移するときに行が TopAppBar へ広がって見えるようにする。
 * 現在はLMUの全項目が対象。対象外の項目には何も付けない。
 * 共有要素のキーは項目ごとに分け、一覧の各行のうち開いた項目の行だけが詳細と結ばれるようにする。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal class AutoReadoutSharedBounds(
    private val sharedTransitionScope: SharedTransitionScope,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
) {
    @Composable
    fun modifier(item: ReadoutItemKey): Modifier =
        if (item is LmuWindowsReadoutItemKey) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("auto-readout-shared-bounds-${item.value}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        } else {
            Modifier
        }
}
