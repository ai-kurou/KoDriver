package kurou.kodriver.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * KoDriver アプリ全体の余白定義。
 *
 * feature モジュール側では padding・Spacer・Arrangement.spacedBy 等の余白値を直接指定せず、
 * `KoDriverSpacing.*` を参照すること。
 */
object KoDriverSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
}
