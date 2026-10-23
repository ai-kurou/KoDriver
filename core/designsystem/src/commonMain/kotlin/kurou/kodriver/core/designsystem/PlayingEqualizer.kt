package kurou.kodriver.core.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val EQUALIZER_HEIGHT = 16.dp
private val EQUALIZER_BAR_WIDTH = 3.dp
private val EQUALIZER_BAR_SPACING = 3.dp
private val EQUALIZER_MIN_HEIGHT = 4.dp
private const val EQUALIZER_HALF_CYCLE_MS = 400
private const val EQUALIZER_BAR_DELAY_MS = 150

/** 再生中であることを示す、3本のアニメーションバーを表示する。 */
@Composable
fun PlayingEqualizer(
    contentDescription: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    val transition = rememberInfiniteTransition(label = "playingEqualizer")
    Row(
        horizontalArrangement = Arrangement.spacedBy(EQUALIZER_BAR_SPACING),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.height(EQUALIZER_HEIGHT).semantics { this.contentDescription = contentDescription },
    ) {
        repeat(3) { index ->
            val fraction by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(EQUALIZER_HALF_CYCLE_MS),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * EQUALIZER_BAR_DELAY_MS),
                    ),
                label = "bar$index",
            )
            Box(
                Modifier
                    .width(EQUALIZER_BAR_WIDTH)
                    .height(EQUALIZER_MIN_HEIGHT + (EQUALIZER_HEIGHT - EQUALIZER_MIN_HEIGHT) * fraction)
                    .background(color, MaterialTheme.shapes.extraSmall),
            )
        }
    }
}
