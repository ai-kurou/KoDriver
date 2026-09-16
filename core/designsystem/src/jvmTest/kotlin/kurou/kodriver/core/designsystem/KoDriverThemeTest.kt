package kurou.kodriver.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KoDriverThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `darkTheme=falseでクラッシュしない`() {
        composeRule.setContent {
            KoDriverTheme(darkTheme = false) {}
        }
    }

    @Test
    fun `darkTheme=trueでクラッシュしない`() {
        composeRule.setContent {
            KoDriverTheme(darkTheme = true) {}
        }
    }

    @Test
    fun `ライトテーマの背景とカードは低彩度のニュートラルを使う`() {
        val colorScheme = captureColorScheme(darkTheme = false)

        assertEquals(Neutral98, colorScheme.background)
        assertEquals(Neutral98, colorScheme.surface)
        assertEquals(Neutral96, colorScheme.surfaceContainerLow)
        assertEquals(Neutral100, colorScheme.surfaceContainerLowest)
    }

    @Test
    fun `ダークテーマの背景とカードは低彩度のニュートラルを使う`() {
        val colorScheme = captureColorScheme(darkTheme = true)

        assertEquals(Neutral6, colorScheme.background)
        assertEquals(Neutral6, colorScheme.surface)
        assertEquals(Neutral10, colorScheme.surfaceContainerLow)
        assertEquals(Neutral4, colorScheme.surfaceContainerLowest)
    }

    @Test
    fun `ライトテーマのsurface系ロールはM3ベースラインの既定値を使わない`() {
        assertSurfaceRolesOverridden(expected = captureColorScheme(darkTheme = false), baseline = lightColorScheme())
    }

    @Test
    fun `ダークテーマのsurface系ロールはM3ベースラインの既定値を使わない`() {
        assertSurfaceRolesOverridden(expected = captureColorScheme(darkTheme = true), baseline = darkColorScheme())
    }

    private fun captureColorScheme(darkTheme: Boolean): ColorScheme {
        var colorScheme: ColorScheme? = null
        composeRule.setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        composeRule.waitForIdle()
        return colorScheme ?: error("MaterialTheme color scheme was not captured.")
    }

    private fun assertSurfaceRolesOverridden(
        expected: ColorScheme,
        baseline: ColorScheme,
    ) {
        assertNotEquals(baseline.surfaceVariant, expected.surfaceVariant)
        assertNotEquals(baseline.onSurfaceVariant, expected.onSurfaceVariant)
        assertNotEquals(baseline.inverseSurface, expected.inverseSurface)
        assertNotEquals(baseline.inverseOnSurface, expected.inverseOnSurface)
        assertNotEquals(baseline.outline, expected.outline)
        assertNotEquals(baseline.outlineVariant, expected.outlineVariant)
        assertNotEquals(baseline.surfaceBright, expected.surfaceBright)
        assertNotEquals(baseline.surfaceDim, expected.surfaceDim)
        assertNotEquals(baseline.surfaceContainerLow, expected.surfaceContainerLow)
        assertNotEquals(baseline.surfaceContainer, expected.surfaceContainer)
        assertNotEquals(baseline.surfaceContainerHigh, expected.surfaceContainerHigh)
        assertNotEquals(baseline.surfaceContainerHighest, expected.surfaceContainerHighest)
    }
}
