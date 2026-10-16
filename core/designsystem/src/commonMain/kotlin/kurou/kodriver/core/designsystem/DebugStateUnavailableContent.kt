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

/** 未取得の文言と装飾を表示する。文言は呼び出し側で解決する。 */
@Composable
fun DebugStateUnavailableContent(
    text: String,
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
