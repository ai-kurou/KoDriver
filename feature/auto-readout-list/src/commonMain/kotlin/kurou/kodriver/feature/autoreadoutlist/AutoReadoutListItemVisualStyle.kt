package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** カテゴリ色をタイルの背景に使うときの不透明度。 */
internal const val CATEGORY_TILE_ALPHA = 0.2f

internal data class AutoReadoutListItemVisualStyle(
    val rankColor: Color,
    val accentColor: Color,
    val accentAlpha: Float,
    val tileColor: Color,
    val tileContentColor: Color,
    /** ON のスイッチのトラック色。カテゴリ色がない項目は null で、スイッチ標準の色にする。 */
    val switchTrackColor: Color?,
)

/**
 * 一覧の項目カードの配色。[categoryColor] を渡すと、ON のときの帯・タイル・スイッチをカテゴリ色にする。
 * カテゴリを持たない項目は null を渡し、従来どおり primary 系の色にする。OFF のときはカテゴリに関係なく無効色にする。
 */
internal fun autoReadoutListItemVisualStyle(
    enabled: Boolean,
    colors: ColorScheme,
    categoryColor: Color?,
): AutoReadoutListItemVisualStyle =
    when {
        !enabled -> {
            AutoReadoutListItemVisualStyle(
                rankColor = colors.onSurfaceVariant,
                accentColor = colors.primary,
                accentAlpha = 0f,
                tileColor = colors.surfaceContainerHighest,
                tileContentColor = colors.onSurfaceVariant,
                switchTrackColor = null,
            )
        }

        categoryColor == null -> {
            AutoReadoutListItemVisualStyle(
                rankColor = colors.onSurface,
                accentColor = colors.primary,
                accentAlpha = 1f,
                tileColor = colors.primaryContainer,
                tileContentColor = colors.onPrimaryContainer,
                switchTrackColor = null,
            )
        }

        else -> {
            AutoReadoutListItemVisualStyle(
                rankColor = colors.onSurface,
                accentColor = categoryColor,
                accentAlpha = 1f,
                tileColor = categoryColor.copy(alpha = CATEGORY_TILE_ALPHA),
                tileContentColor = categoryColor,
                switchTrackColor = categoryColor,
            )
        }
    }
