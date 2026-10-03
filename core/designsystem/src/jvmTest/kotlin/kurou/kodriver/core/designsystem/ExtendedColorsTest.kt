package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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

    @Test
    fun `ヒート色はライトとダークで別の値を持ち温度段階ごとに異なる`() {
        listOf(LightExtendedColorScheme, DarkExtendedColorScheme).forEach { scheme ->
            assertEquals(
                4,
                setOf(scheme.heatCool, scheme.heatOk, scheme.heatWarm, scheme.heatHot).size,
            )
        }
        assertNotEquals(LightExtendedColorScheme.heatHot, DarkExtendedColorScheme.heatHot)
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
