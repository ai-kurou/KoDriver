package kurou.kodriver.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme =
    lightColorScheme(
        primary = Yellow40,
        onPrimary = Neutral100,
        primaryContainer = Yellow90,
        onPrimaryContainer = Yellow10,
        inversePrimary = Yellow80,
        secondary = Secondary40,
        onSecondary = Neutral100,
        secondaryContainer = Secondary90,
        onSecondaryContainer = Secondary10,
        tertiary = Tertiary40,
        onTertiary = Neutral100,
        tertiaryContainer = Tertiary90,
        onTertiaryContainer = Tertiary10,
        error = Error40,
        onError = Neutral100,
        errorContainer = Error90,
        onErrorContainer = Error10,
        background = Neutral98,
        onBackground = Neutral10,
        surface = Neutral98,
        onSurface = Neutral10,
        surfaceVariant = NeutralVariant90,
        onSurfaceVariant = NeutralVariant30,
        inverseSurface = Neutral20,
        inverseOnSurface = Neutral95,
        outline = NeutralVariant50,
        outlineVariant = NeutralVariant80,
        surfaceBright = Neutral98,
        surfaceDim = Neutral87,
        surfaceContainerLowest = Neutral100,
        surfaceContainerLow = Neutral96,
        surfaceContainer = Neutral94,
        surfaceContainerHigh = Neutral92,
        surfaceContainerHighest = Neutral90,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Yellow80,
        onPrimary = Yellow20,
        primaryContainer = Yellow30,
        onPrimaryContainer = Yellow90,
        inversePrimary = Yellow40,
        secondary = Secondary80,
        onSecondary = Secondary20,
        secondaryContainer = Secondary30,
        onSecondaryContainer = Secondary90,
        tertiary = Tertiary80,
        onTertiary = Tertiary20,
        tertiaryContainer = Tertiary30,
        onTertiaryContainer = Tertiary90,
        error = Error80,
        onError = Error20,
        errorContainer = Error30,
        onErrorContainer = Error90,
        background = Neutral6,
        onBackground = Neutral90,
        surface = Neutral6,
        onSurface = Neutral90,
        surfaceVariant = NeutralVariant30,
        onSurfaceVariant = NeutralVariant80,
        inverseSurface = Neutral90,
        inverseOnSurface = Neutral20,
        outline = NeutralVariant60,
        outlineVariant = NeutralVariant30,
        surfaceBright = Neutral24,
        surfaceDim = Neutral6,
        surfaceContainerLowest = Neutral4,
        surfaceContainerLow = Neutral10,
        surfaceContainer = Neutral12,
        surfaceContainerHigh = Neutral17,
        surfaceContainerHighest = Neutral22,
    )

/**
 * KoDriverTheme を提供する公開関数。
 */
@Composable
fun KoDriverTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtendedColorScheme provides if (darkTheme) DarkExtendedColorScheme else LightExtendedColorScheme,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = KoDriverTypography,
            shapes = KoDriverShapes,
            content = content,
        )
    }
}
