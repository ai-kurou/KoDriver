@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AppThemeAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(sdk = [30])
    fun `SDK31未満では動的カラースキームを返さない`() {
        captureDynamicColorScheme(darkTheme = false)
            .also(::assertNull)
    }

    @Test
    @Config(sdk = [36])
    fun `SDK31以上ではライトテーマの動的カラースキームを返す`() {
        val (actual, expected) = captureDynamicColorSchemeComparison(darkTheme = false)

        assertColorSchemeEquals(expected, actual)
    }

    @Test
    @Config(sdk = [36])
    fun `SDK31以上ではダークテーマの動的カラースキームを返す`() {
        val (actual, expected) = captureDynamicColorSchemeComparison(darkTheme = true)

        assertColorSchemeEquals(expected, actual)
    }

    @Test
    @Config(sdk = [30])
    fun `動的カラーが無効ならライトテーマのフォールバックカラースキームを使う`() {
        val fallbackPrimary = captureAppThemePrimary(darkTheme = false, dynamicColor = false)

        assertTrue(Color(0xFF4C6600).value == fallbackPrimary)
    }

    @Test
    @Config(sdk = [30])
    fun `動的カラーが無効ならダークテーマのフォールバックカラースキームを使う`() {
        val fallbackPrimary = captureAppThemePrimary(darkTheme = true, dynamicColor = false)

        assertTrue(Color(0xFFBFFF00).value == fallbackPrimary)
    }

    @Test
    @Config(sdk = [30])
    fun `SDK31未満で動的カラーが有効でもフォールバックカラースキームを使う`() {
        val fallbackPrimary = captureAppThemePrimary(darkTheme = false, dynamicColor = true)

        assertTrue(Color(0xFF4C6600).value == fallbackPrimary)
    }

    private fun captureDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
        var colorScheme: ColorScheme? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalContext provides ApplicationProvider.getApplicationContext()) {
                colorScheme = dynamicAppColorScheme(darkTheme)
            }
        }
        composeRule.waitForIdle()
        return colorScheme
    }

    private fun captureDynamicColorSchemeComparison(darkTheme: Boolean): Pair<ColorScheme?, ColorScheme?> {
        var actual: ColorScheme? = null
        var expected: ColorScheme? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalContext provides ApplicationProvider.getApplicationContext()) {
                val context = LocalContext.current
                actual = dynamicAppColorScheme(darkTheme)
                expected = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
        }
        composeRule.waitForIdle()
        return actual to expected
    }

    private fun assertColorSchemeEquals(
        expected: ColorScheme?,
        actual: ColorScheme?,
    ) {
        assertNotNull(expected)
        assertNotNull(actual)
        assertEquals(expected.primary.value, actual.primary.value)
        assertEquals(expected.onPrimary.value, actual.onPrimary.value)
        assertEquals(expected.primaryContainer.value, actual.primaryContainer.value)
        assertEquals(expected.onPrimaryContainer.value, actual.onPrimaryContainer.value)
        assertEquals(expected.inversePrimary.value, actual.inversePrimary.value)
        assertEquals(expected.secondary.value, actual.secondary.value)
        assertEquals(expected.onSecondary.value, actual.onSecondary.value)
        assertEquals(expected.secondaryContainer.value, actual.secondaryContainer.value)
        assertEquals(expected.onSecondaryContainer.value, actual.onSecondaryContainer.value)
        assertEquals(expected.tertiary.value, actual.tertiary.value)
        assertEquals(expected.onTertiary.value, actual.onTertiary.value)
        assertEquals(expected.tertiaryContainer.value, actual.tertiaryContainer.value)
        assertEquals(expected.onTertiaryContainer.value, actual.onTertiaryContainer.value)
        assertEquals(expected.background.value, actual.background.value)
        assertEquals(expected.onBackground.value, actual.onBackground.value)
        assertEquals(expected.surface.value, actual.surface.value)
        assertEquals(expected.onSurface.value, actual.onSurface.value)
        assertEquals(expected.surfaceVariant.value, actual.surfaceVariant.value)
        assertEquals(expected.onSurfaceVariant.value, actual.onSurfaceVariant.value)
        assertEquals(expected.surfaceTint.value, actual.surfaceTint.value)
        assertEquals(expected.inverseSurface.value, actual.inverseSurface.value)
        assertEquals(expected.inverseOnSurface.value, actual.inverseOnSurface.value)
        assertEquals(expected.error.value, actual.error.value)
        assertEquals(expected.onError.value, actual.onError.value)
        assertEquals(expected.errorContainer.value, actual.errorContainer.value)
        assertEquals(expected.onErrorContainer.value, actual.onErrorContainer.value)
        assertEquals(expected.outline.value, actual.outline.value)
        assertEquals(expected.outlineVariant.value, actual.outlineVariant.value)
        assertEquals(expected.scrim.value, actual.scrim.value)
        assertEquals(expected.surfaceBright.value, actual.surfaceBright.value)
        assertEquals(expected.surfaceDim.value, actual.surfaceDim.value)
        assertEquals(expected.surfaceContainer.value, actual.surfaceContainer.value)
        assertEquals(expected.surfaceContainerHigh.value, actual.surfaceContainerHigh.value)
        assertEquals(expected.surfaceContainerHighest.value, actual.surfaceContainerHighest.value)
        assertEquals(expected.surfaceContainerLow.value, actual.surfaceContainerLow.value)
        assertEquals(expected.surfaceContainerLowest.value, actual.surfaceContainerLowest.value)
    }

    private fun captureAppThemePrimary(
        darkTheme: Boolean,
        dynamicColor: Boolean,
    ): ULong {
        var primary: ULong? = null
        composeRule.setContent {
            AppTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
            ) {
                primary = MaterialTheme.colorScheme.primary.value
            }
        }
        composeRule.waitForIdle()
        return primary ?: error("MaterialTheme color scheme was not captured.")
    }
}
