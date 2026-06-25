package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `読み上げタブを再タップするとonReadoutTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNodeWithTag("nav_readout").performClick()
        rule.waitForIdle()

        assertEquals(1, readoutReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `その他タブを再タップするとonOtherTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()
        assertEquals(0, otherReselectedCount)

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(1, otherReselectedCount)
    }

    @Test
    fun `別タブに切り替えてもreselectedコールバックは呼ばれない`() {
        var readoutReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("nav_readout").performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, otherReselectedCount)
    }
}
