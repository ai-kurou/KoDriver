package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Yellow40,
    onPrimary = Neutral99,
    primaryContainer = Yellow90,
    onPrimaryContainer = Yellow10,

    secondary = Amber40,
    onSecondary = Neutral99,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber10,

    tertiary = Neon40,
    onTertiary = Neutral99,
    tertiaryContainer = Neon80,
    onTertiaryContainer = Neon10,

    error = Error40,
    onError = Neutral99,
    errorContainer = Error90,
    onErrorContainer = Error10,

    background = Yellow99,
    onBackground = Neutral10,
    surface = Yellow99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    surfaceContainerLow = Yellow95,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = NeutralVariant90,
)

private val DarkColorScheme = darkColorScheme(
    primary = Yellow80,
    onPrimary = Yellow20,
    primaryContainer = Yellow30,
    onPrimaryContainer = Yellow90,

    secondary = Amber80,
    onSecondary = Amber20,
    secondaryContainer = Amber30,
    onSecondaryContainer = Amber90,

    tertiary = Neon80,
    onTertiary = Neon20,
    tertiaryContainer = Neon30,
    onTertiaryContainer = Neon90,

    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,

    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    surfaceContainerLow = Neutral20,
    surfaceContainer = Neutral20,
    surfaceContainerHigh = NeutralVariant30,
)

@Composable
fun KoDriverTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
