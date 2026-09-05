package kurou.kodriver.core.designsystem

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class TypographyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `koDriverNumericTextStyleはtabular figuresを有効にする`() {
        var actual: TextStyle? = null
        composeRule.setContent {
            MaterialTheme {
                actual = koDriverNumericTextStyle()
            }
        }

        assertEquals("tnum", actual?.fontFeatureSettings)
    }

    @Test
    fun `koDriverNumericTextStyleは呼び出し時点のLocalTextStyleを引き継ぐ`() {
        var actual: TextStyle? = null
        composeRule.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalTextStyle provides TextStyle(fontSize = 20.sp)) {
                    actual = koDriverNumericTextStyle()
                }
            }
        }

        assertEquals(20.sp, actual?.fontSize)
        assertEquals("tnum", actual?.fontFeatureSettings)
    }
}
