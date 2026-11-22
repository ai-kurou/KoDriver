package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

private const val FLAG_SHARED_BOUNDS_KEY = "auto-readout-flag-shared-bounds"

/**
 * 一覧の行と詳細の TopAppBar を共有要素として結び、一覧から詳細へ遷移するときに行が TopAppBar へ広がって見えるようにする。
 * 現在はLMUのフラッグのみが対象。対象外の項目には何も付けない。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal class AutoReadoutSharedBounds(
    private val sharedTransitionScope: SharedTransitionScope,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
) {
    @Composable
    fun modifier(item: ReadoutItemKey): Modifier =
        if (item == LmuWindowsReadoutItemKey.Flag.Root) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(FLAG_SHARED_BOUNDS_KEY),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        } else {
            Modifier
        }
}
