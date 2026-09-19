package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * `MaterialTheme.colorScheme` にロールがない拡張カラー（警告色等）をまとめたデータクラス。
 *
 * primary/secondary/tertiary のいずれとも意味が異なる用途（例: 警告表示）に使う色をここへ追加する。
 */
data class ExtendedColorScheme(
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val LightExtendedColorScheme =
    ExtendedColorScheme(
        warningContainer = Color(0xFFFFF9C4),
        onWarningContainer = Color(0xFF5F4B00),
    )

internal val DarkExtendedColorScheme =
    ExtendedColorScheme(
        warningContainer = Color(0xFF5C4700),
        onWarningContainer = Color(0xFFFFE8A3),
    )

internal val LocalExtendedColorScheme = staticCompositionLocalOf { LightExtendedColorScheme }

/**
 * [KoDriverTheme] 配下で拡張カラーを参照するためのエントリポイント。
 *
 * `MaterialTheme.colorScheme` と同様に、Composable 内で `KoDriverExtendedColors.current` として参照する。
 */
object KoDriverExtendedColors {
    val current: ExtendedColorScheme
        @Composable
        get() = LocalExtendedColorScheme.current
}
