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
    val heatCool: Color,
    val heatOk: Color,
    val heatWarm: Color,
    val heatHot: Color,
)

internal val LightExtendedColorScheme =
    ExtendedColorScheme(
        warningContainer = Color(0xFFFFF9C4),
        onWarningContainer = Color(0xFF5F4B00),
        heatCool = Color(0xFF4A8FD0),
        heatOk = Color(0xFF5A9A2A),
        heatWarm = Color(0xFFE0A020),
        heatHot = Color(0xFFD9482B),
    )

internal val DarkExtendedColorScheme =
    ExtendedColorScheme(
        warningContainer = Color(0xFF5C4700),
        onWarningContainer = Color(0xFFFFE8A3),
        heatCool = Color(0xFF7DB4E6),
        heatOk = Color(0xFF8CC85A),
        heatWarm = Color(0xFFF0C050),
        heatHot = Color(0xFFF08060),
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
