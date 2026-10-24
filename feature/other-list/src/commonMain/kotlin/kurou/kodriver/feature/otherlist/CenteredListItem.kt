package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing

private val centeredListItemMinHeight = 56.dp

/**
 * M3 の ListItem 相当の見た目で、先頭・末尾のアイコンを常に縦中央に揃える一覧項目。
 *
 * M3 の ListItem は副テキストが折り返して3行になると先頭・末尾の要素を上揃えにするため、
 * 狭いペインで音量の副テキストが折り返したときにアイコンが行の中央から外れる。それを避けるため自前で組む。
 */
@Composable
internal fun CenteredListItem(
    headlineContent: @Composable () -> Unit,
    leadingContent: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit,
    containerColor: Color,
    headlineColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .background(containerColor)
                .heightIn(min = centeredListItemMinHeight)
                .padding(
                    horizontal = KoDriverSpacing.large,
                    vertical = KoDriverSpacing.small,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.large),
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box { leadingContent() }
        }
        Column(modifier = Modifier.weight(1f)) {
            CompositionLocalProvider(LocalContentColor provides headlineColor) {
                ProvideTextStyle(MaterialTheme.typography.bodyLarge, content = headlineContent)
            }
            if (supportingContent != null) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium, content = supportingContent)
                }
            }
        }
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box { trailingContent() }
        }
    }
}
