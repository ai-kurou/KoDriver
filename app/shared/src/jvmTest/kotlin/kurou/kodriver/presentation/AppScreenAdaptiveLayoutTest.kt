package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.window.core.layout.WindowSizeClass
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenAdaptiveLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `largeとextraLarge幅ではNavigationRailを使用する`() {
        listOf(1200, 1600).forEach { width ->
            val layoutType = WindowSizeClass(minWidthDp = width, minHeightDp = 0).resolveNavigationSuiteType()

            assertEquals(NavigationSuiteType.NavigationRail, layoutType)
        }
    }
}
