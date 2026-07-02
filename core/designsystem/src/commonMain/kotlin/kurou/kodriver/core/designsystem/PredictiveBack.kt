package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

typealias AppBackHandler = @Composable (
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
) -> Unit

fun Modifier.predictiveBackDetailPane(progress: Float): Modifier = graphicsLayer {
    val coercedProgress = progress.coerceIn(0f, 1f)
    translationX = size.width * 0.25f * coercedProgress
}
