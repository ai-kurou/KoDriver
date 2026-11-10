package kurou.kodriver.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

private const val ACCENT_FILL_ALPHA = 0.14f
private const val ACCENT_BORDER_ALPHA = 0.28f

/**
 * 語句を1つ表示する小さなチップ。音声タブの語句一覧と、音声認識の認識結果で同じ見た目に揃える。
 * [accent] を渡すと、その色で薄く染めて細い枠を付ける（音声タブの語句一覧でカテゴリ色を見せるため）。
 */
@Composable
fun KoDriverPhraseChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = accent?.copy(alpha = ACCENT_FILL_ALPHA)?.compositeOver(base) ?: base,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = accent?.let { BorderStroke(1.dp, it.copy(alpha = ACCENT_BORDER_ALPHA)) },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        )
    }
}
