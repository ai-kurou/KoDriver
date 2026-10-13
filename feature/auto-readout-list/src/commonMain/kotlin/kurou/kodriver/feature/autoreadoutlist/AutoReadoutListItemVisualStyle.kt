package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal data class AutoReadoutListItemVisualStyle(
    val rankColor: Color,
    val accentAlpha: Float,
    val tileColor: Color,
    val tileContentColor: Color,
)

internal fun autoReadoutListItemVisualStyle(
    enabled: Boolean,
    colors: ColorScheme,
): AutoReadoutListItemVisualStyle =
    if (enabled) {
        AutoReadoutListItemVisualStyle(
            rankColor = colors.onSurface,
            accentAlpha = 1f,
            tileColor = colors.primaryContainer,
            tileContentColor = colors.onPrimaryContainer,
        )
    } else {
        AutoReadoutListItemVisualStyle(
            rankColor = colors.onSurfaceVariant,
            accentAlpha = 0f,
            tileColor = colors.surfaceContainerHighest,
            tileContentColor = colors.onSurfaceVariant,
        )
    }
