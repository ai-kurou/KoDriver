package kurou.kodriver.core.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * KoDriver アプリ全体の余白定義。
 *
 * feature モジュール側では padding・Spacer・Arrangement.spacedBy 等の余白値を直接指定せず、
 * `KoDriverSpacing.*` を参照すること。
 */
public object KoDriverSpacing {
    public val extraSmall: Dp = 4.dp
    public val small: Dp = 8.dp
    public val medium: Dp = 12.dp
    public val large: Dp = 16.dp
    public val extraLarge: Dp = 24.dp
}
