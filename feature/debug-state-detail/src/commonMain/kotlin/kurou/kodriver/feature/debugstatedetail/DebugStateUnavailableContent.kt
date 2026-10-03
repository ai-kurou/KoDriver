package kurou.kodriver.feature.debugstatedetail

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
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DebugStateUnavailableContent() {
    val color = MaterialTheme.colorScheme.outlineVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Text(
            text = stringResource(Res.string.debug_state_flag_info_unavailable),
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
