package kurou.kodriver.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/** 色と装飾記号を呼び出し側で決めるデバッグカードの共通枠。 */
@Composable
fun DebugStateCardFrame(
    title: String,
    symbol: String,
    accent: Color,
    modifier: Modifier = Modifier,
    showHandle: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLow) {
        Column(
            modifier =
                Modifier
                    .drawBehind {
                        drawRect(accent, size = Size(4.dp.toPx(), size.height))
                    }.padding(KoDriverSpacing.large),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(accent.copy(alpha = 0.14f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    // タイトルと重複する装飾のため、スクリーンリーダーの読み上げ対象から外す。
                    Text(
                        text = symbol,
                        modifier = Modifier.clearAndSetSemantics {},
                        color = accent,
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                )
                if (showHandle) {
                    Canvas(Modifier.size(16.dp, 24.dp)) {
                        repeat(2) { column ->
                            repeat(3) { row ->
                                drawCircle(
                                    colors.onSurfaceVariant.copy(alpha = 0.35f),
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(size.width * (column + 1) / 3, size.height * (row + 1) / 4),
                                )
                            }
                        }
                    }
                }
            }
            content()
        }
    }
}
