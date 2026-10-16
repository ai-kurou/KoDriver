package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val DISABLED_CARD_ALPHA = 0.38f

/** キーに対応するタイトルと内容を表示し、有効状態に応じた透明度を共通の枠へ渡すデバッグカード。 */
@Suppress("UnstableCollections")
@Composable
fun <K, S> DebugStateCard(
    cardKey: K,
    uiState: S,
    enabled: Boolean,
    titles: Map<K, StringResource>,
    contents: Map<K, @Composable (S) -> Unit>,
    frame: @Composable (title: String, modifier: Modifier, content: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    frame(
        stringResource(titles.getValue(cardKey)),
        modifier.alpha(if (enabled) 1f else DISABLED_CARD_ALPHA),
        { contents.getValue(cardKey)(uiState) },
    )
}
