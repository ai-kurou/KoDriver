package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ExtendedColorsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `KoDriverThemeはライトテーマでLightExtendedColorSchemeを提供する`() {
        val extendedColorScheme = captureExtendedColorScheme(darkTheme = false)

        assertEquals(LightExtendedColorScheme, extendedColorScheme)
    }

    @Test
    fun `KoDriverThemeはダークテーマでDarkExtendedColorSchemeを提供する`() {
        val extendedColorScheme = captureExtendedColorScheme(darkTheme = true)

        assertEquals(DarkExtendedColorScheme, extendedColorScheme)
    }

    private fun captureExtendedColorScheme(darkTheme: Boolean): ExtendedColorScheme {
        var extendedColorScheme: ExtendedColorScheme? = null
        composeRule.setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                extendedColorScheme = KoDriverExtendedColors.current
            }
        }
        composeRule.waitForIdle()
        return extendedColorScheme ?: error("Extended color scheme was not captured.")
    }
}
