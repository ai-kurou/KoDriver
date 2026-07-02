package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppYellow10 = Color(0xFF121900)
private val AppYellow20 = Color(0xFF253300)
private val AppYellow30 = Color(0xFF384D00)
private val AppYellow40 = Color(0xFF4C6600)
private val AppYellow80 = Color(0xFFBFFF00)
private val AppYellow90 = Color(0xFFE0FF8C)
private val AppYellow95 = Color(0xFFEFFFBB)
private val AppYellow99 = Color(0xFFF8FFED)

private val AppLime10 = Color(0xFF0D1A00)
private val AppLime20 = Color(0xFF1A3300)
private val AppLime30 = Color(0xFF284D00)
private val AppLime40 = Color(0xFF366600)
private val AppLime80 = Color(0xFF80FF4D)
private val AppLime90 = Color(0xFFB8FFAA)

private val AppNeon10 = Color(0xFF0A1400)
private val AppNeon20 = Color(0xFF152800)
private val AppNeon30 = Color(0xFF213D00)
private val AppNeon40 = Color(0xFF2D5200)
private val AppNeon80 = Color(0xFFD4FF00)
private val AppNeon90 = Color(0xFFEAFF80)

private val AppError10 = Color(0xFF410002)
private val AppError20 = Color(0xFF690005)
private val AppError30 = Color(0xFF93000A)
private val AppError40 = Color(0xFFBA1A1A)
private val AppError80 = Color(0xFFFFB4AB)
private val AppError90 = Color(0xFFFFDAD6)

private val AppNeutral10 = Color(0xFF1C1B1E)
private val AppNeutral20 = Color(0xFF313033)
private val AppNeutral90 = Color(0xFFE6E1E5)
private val AppNeutral95 = Color(0xFFF4EFF4)
private val AppNeutral99 = Color(0xFFFFFBFE)

private val AppNeutralVariant30 = Color(0xFF4A4458)
private val AppNeutralVariant50 = Color(0xFF79747E)
private val AppNeutralVariant60 = Color(0xFF938F99)
private val AppNeutralVariant80 = Color(0xFFCAC4D0)
private val AppNeutralVariant90 = Color(0xFFE7E0EC)

private val AppLightColorScheme = lightColorScheme(
    primary = AppYellow40,
    onPrimary = AppNeutral99,
    primaryContainer = AppYellow90,
    onPrimaryContainer = AppYellow10,

    secondary = AppLime40,
    onSecondary = AppNeutral99,
    secondaryContainer = AppLime90,
    onSecondaryContainer = AppLime10,

    tertiary = AppNeon40,
    onTertiary = AppNeutral99,
    tertiaryContainer = AppNeon80,
    onTertiaryContainer = AppNeon10,

    error = AppError40,
    onError = AppNeutral99,
    errorContainer = AppError90,
    onErrorContainer = AppError10,

    background = AppYellow99,
    onBackground = AppNeutral10,
    surface = AppYellow99,
    onSurface = AppNeutral10,
    surfaceVariant = AppNeutralVariant90,
    onSurfaceVariant = AppNeutralVariant30,
    outline = AppNeutralVariant50,
    outlineVariant = AppNeutralVariant80,
    surfaceContainerLow = AppYellow95,
    surfaceContainer = AppNeutral95,
    surfaceContainerHigh = AppNeutralVariant90,
)

private val AppDarkColorScheme = darkColorScheme(
    primary = AppYellow80,
    onPrimary = AppYellow20,
    primaryContainer = AppYellow30,
    onPrimaryContainer = AppYellow90,

    secondary = AppLime80,
    onSecondary = AppLime20,
    secondaryContainer = AppLime30,
    onSecondaryContainer = AppLime90,

    tertiary = AppNeon80,
    onTertiary = AppNeon20,
    tertiaryContainer = AppNeon30,
    onTertiaryContainer = AppNeon90,

    error = AppError80,
    onError = AppError20,
    errorContainer = AppError30,
    onErrorContainer = AppError90,

    background = AppNeutral10,
    onBackground = AppNeutral90,
    surface = AppNeutral10,
    onSurface = AppNeutral90,
    surfaceVariant = AppNeutralVariant30,
    onSurfaceVariant = AppNeutralVariant80,
    outline = AppNeutralVariant60,
    outlineVariant = AppNeutralVariant30,
    surfaceContainerLow = AppNeutral20,
    surfaceContainer = AppNeutral20,
    surfaceContainerHigh = AppNeutralVariant30,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme,
        content = content,
    )
}
