package kurou.kodriver.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// app:shared は moduleGraphAssert で core:.* への依存が禁止されているため、
// core:designsystem の KoDriverTheme（Color.kt/Theme.kt/Shapes.kt）と同じ配色値・角丸をここに複製している。
// 配色・角丸を変更する場合は両方を同期させること。

// Primary – Neon Yellow-Green（蛍光黄緑）。ブランド色として選択状態・スイッチ・強調にのみ使う。
private val AppYellow10 = Color(0xFF121900)
private val AppYellow20 = Color(0xFF253300)
private val AppYellow30 = Color(0xFF384D00)
private val AppYellow40 = Color(0xFF4C6600)
private val AppYellow80 = Color(0xFFBFFF00)
private val AppYellow90 = Color(0xFFE0FF8C)

// Secondary – 低彩度のオリーブ。primary と同系色相のまま彩度を落として役割を分ける。
private val AppSecondary10 = Color(0xFF161E0A)
private val AppSecondary20 = Color(0xFF2B331E)
private val AppSecondary30 = Color(0xFF414A33)
private val AppSecondary40 = Color(0xFF586249)
private val AppSecondary80 = Color(0xFFC0CAAD)
private val AppSecondary90 = Color(0xFFDCE7C8)

// Tertiary – ティール。primary と色相を分けたアクセント。
private val AppTertiary10 = Color(0xFF00201E)
private val AppTertiary20 = Color(0xFF003734)
private val AppTertiary30 = Color(0xFF1E4E4B)
private val AppTertiary40 = Color(0xFF386663)
private val AppTertiary80 = Color(0xFFA0CFCB)
private val AppTertiary90 = Color(0xFFBCECE7)

// Error
private val AppError10 = Color(0xFF410002)
private val AppError20 = Color(0xFF690005)
private val AppError30 = Color(0xFF93000A)
private val AppError40 = Color(0xFFBA1A1A)
private val AppError80 = Color(0xFFFFB4AB)
private val AppError90 = Color(0xFFFFDAD6)

// Neutral – わずかに緑寄りの低彩度グレー（背景・surface 系）
private val AppNeutral4 = Color(0xFF0C0E0B)
private val AppNeutral6 = Color(0xFF111310)
private val AppNeutral10 = Color(0xFF1A1C18)
private val AppNeutral12 = Color(0xFF1D1F1B)
private val AppNeutral17 = Color(0xFF272925)
private val AppNeutral20 = Color(0xFF2F312D)
private val AppNeutral22 = Color(0xFF333531)
private val AppNeutral24 = Color(0xFF373935)
private val AppNeutral87 = Color(0xFFD9DAD4)
private val AppNeutral90 = Color(0xFFE2E3DD)
private val AppNeutral92 = Color(0xFFE8E9E2)
private val AppNeutral94 = Color(0xFFEDEEE8)
private val AppNeutral95 = Color(0xFFF0F1EB)
private val AppNeutral96 = Color(0xFFF3F4EE)
private val AppNeutral98 = Color(0xFFF9FAF4)
private val AppNeutral100 = Color(0xFFFFFFFF)

// NeutralVariant – わずかに緑寄りの低彩度グレー（区切り線・補助テキスト系）
private val AppNeutralVariant30 = Color(0xFF45483D)
private val AppNeutralVariant50 = Color(0xFF75786C)
private val AppNeutralVariant60 = Color(0xFF8F9285)
private val AppNeutralVariant80 = Color(0xFFC5C8B9)
private val AppNeutralVariant90 = Color(0xFFE1E4D5)

private val AppLightColorScheme =
    lightColorScheme(
        primary = AppYellow40,
        onPrimary = AppNeutral100,
        primaryContainer = AppYellow90,
        onPrimaryContainer = AppYellow10,
        inversePrimary = AppYellow80,
        secondary = AppSecondary40,
        onSecondary = AppNeutral100,
        secondaryContainer = AppSecondary90,
        onSecondaryContainer = AppSecondary10,
        tertiary = AppTertiary40,
        onTertiary = AppNeutral100,
        tertiaryContainer = AppTertiary90,
        onTertiaryContainer = AppTertiary10,
        error = AppError40,
        onError = AppNeutral100,
        errorContainer = AppError90,
        onErrorContainer = AppError10,
        background = AppNeutral98,
        onBackground = AppNeutral10,
        surface = AppNeutral98,
        onSurface = AppNeutral10,
        surfaceVariant = AppNeutralVariant90,
        onSurfaceVariant = AppNeutralVariant30,
        inverseSurface = AppNeutral20,
        inverseOnSurface = AppNeutral95,
        outline = AppNeutralVariant50,
        outlineVariant = AppNeutralVariant80,
        surfaceBright = AppNeutral98,
        surfaceDim = AppNeutral87,
        surfaceContainerLowest = AppNeutral100,
        surfaceContainerLow = AppNeutral96,
        surfaceContainer = AppNeutral94,
        surfaceContainerHigh = AppNeutral92,
        surfaceContainerHighest = AppNeutral90,
    )

private val AppDarkColorScheme =
    darkColorScheme(
        primary = AppYellow80,
        onPrimary = AppYellow20,
        primaryContainer = AppYellow30,
        onPrimaryContainer = AppYellow90,
        inversePrimary = AppYellow40,
        secondary = AppSecondary80,
        onSecondary = AppSecondary20,
        secondaryContainer = AppSecondary30,
        onSecondaryContainer = AppSecondary90,
        tertiary = AppTertiary80,
        onTertiary = AppTertiary20,
        tertiaryContainer = AppTertiary30,
        onTertiaryContainer = AppTertiary90,
        error = AppError80,
        onError = AppError20,
        errorContainer = AppError30,
        onErrorContainer = AppError90,
        background = AppNeutral6,
        onBackground = AppNeutral90,
        surface = AppNeutral6,
        onSurface = AppNeutral90,
        surfaceVariant = AppNeutralVariant30,
        onSurfaceVariant = AppNeutralVariant80,
        inverseSurface = AppNeutral90,
        inverseOnSurface = AppNeutral20,
        outline = AppNeutralVariant60,
        outlineVariant = AppNeutralVariant30,
        surfaceBright = AppNeutral24,
        surfaceDim = AppNeutral6,
        surfaceContainerLowest = AppNeutral4,
        surfaceContainerLow = AppNeutral10,
        surfaceContainer = AppNeutral12,
        surfaceContainerHigh = AppNeutral17,
        surfaceContainerHighest = AppNeutral22,
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
    )

@Composable
internal expect fun dynamicAppColorScheme(darkTheme: Boolean): ColorScheme?

/**
 * AppTheme を提供する公開関数。
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val fallbackColorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
    val colorScheme =
        if (dynamicColor) {
            dynamicAppColorScheme(darkTheme) ?: fallbackColorScheme
        } else {
            fallbackColorScheme
        }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content,
    )
}
