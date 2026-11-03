package kurou.kodriver.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.debug_state_unavailable
import org.jetbrains.compose.resources.stringResource

/** 未取得の文言と装飾を表示する。文言は既定で共通の「未取得」を使う。 */
@Composable
fun DebugStateUnavailableContent(
    text: String = stringResource(Res.string.debug_state_unavailable),
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Canvas(Modifier.weight(1f).height(8.dp)) {
            val step = 14.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawRoundRect(color, topLeft = Offset(x, 0f), size = Size(8.dp.toPx(), size.height))
                x += step
            }
        }
    }
}
