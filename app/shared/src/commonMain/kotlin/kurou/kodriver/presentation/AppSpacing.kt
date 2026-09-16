package kurou.kodriver.presentation

import androidx.compose.ui.unit.dp

// app:shared は moduleGraphAssert で core:.* への依存が禁止されているため、
// core:designsystem の KoDriverSpacing（Spacing.kt）と同じ余白値をここに複製している。
// 余白の値を変更する場合は両方を同期させること。
internal object AppSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
}
