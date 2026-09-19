package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// app:shared は moduleGraphAssert で core:.* への依存が禁止されているため、
// core:designsystem の ExtendedColorScheme（ExtendedColors.kt）と同じ拡張カラー定義をここに複製している。
// 拡張カラーの値を変更する場合は両方を同期させること。

/**
 * `MaterialTheme.colorScheme` にロールがない拡張カラー（警告色等）をまとめたデータクラス。
 */
internal data class AppExtendedColorScheme(
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val AppLightExtendedColorScheme =
    AppExtendedColorScheme(
        warningContainer = Color(0xFFFFF9C4),
        onWarningContainer = Color(0xFF5F4B00),
    )

internal val AppDarkExtendedColorScheme =
    AppExtendedColorScheme(
        warningContainer = Color(0xFF5C4700),
        onWarningContainer = Color(0xFFFFE8A3),
    )

internal val LocalAppExtendedColorScheme = staticCompositionLocalOf { AppLightExtendedColorScheme }

/**
 * [AppTheme] 配下で拡張カラーを参照するためのエントリポイント。
 *
 * `MaterialTheme.colorScheme` と同様に、Composable 内で `AppExtendedColors.current` として参照する。
 */
internal object AppExtendedColors {
    val current: AppExtendedColorScheme
        @Composable
        get() = LocalAppExtendedColorScheme.current
}
