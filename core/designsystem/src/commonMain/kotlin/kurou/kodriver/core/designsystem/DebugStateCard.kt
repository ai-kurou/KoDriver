package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val DISABLED_CARD_ALPHA = 0.38f

/** カードキーに対応するタイトルと内容を解決し、有効状態に応じた透明度で表示する。 */
@Composable
fun <K : Any, S> DebugStateCard(
    cardKey: K,
    uiState: S,
    enabled: Boolean,
    titleOf: (K) -> StringResource,
    contentOf: (K) -> @Composable (S) -> Unit,
    frame: @Composable (cardKey: K, title: String, modifier: Modifier, content: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    frame(
        cardKey,
        stringResource(titleOf(cardKey)),
        modifier.alpha(if (enabled) 1f else DISABLED_CARD_ALPHA),
        {
            contentOf(cardKey)(uiState)
        },
    )
}
