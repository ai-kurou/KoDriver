package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal data class ReadoutListItemVisualStyle(
    val rankColor: Color,
    val accentAlpha: Float,
    val tileColor: Color,
    val tileContentColor: Color,
)

internal fun readoutListItemVisualStyle(
    enabled: Boolean,
    colors: ColorScheme,
): ReadoutListItemVisualStyle =
    if (enabled) {
        ReadoutListItemVisualStyle(
            rankColor = colors.onSurface,
            accentAlpha = 1f,
            tileColor = colors.primaryContainer,
            tileContentColor = colors.onPrimaryContainer,
        )
    } else {
        ReadoutListItemVisualStyle(
            rankColor = colors.onSurfaceVariant.copy(alpha = 0.5f),
            accentAlpha = 0f,
            tileColor = colors.surfaceContainerHighest,
            tileContentColor = colors.onSurfaceVariant,
        )
    }
