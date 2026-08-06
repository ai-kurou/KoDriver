@file:OptIn(ExperimentalTestApi::class)

package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import org.junit.Test

class KoDriverThemeTest {
    @Test
    fun `darkTheme=falseでクラッシュしない`() =
        runDesktopComposeUiTest {
            setContent {
                KoDriverTheme(darkTheme = false) {}
            }
        }

    @Test
    fun `darkTheme=trueでクラッシュしない`() =
        runDesktopComposeUiTest {
            setContent {
                KoDriverTheme(darkTheme = true) {}
            }
        }
}
