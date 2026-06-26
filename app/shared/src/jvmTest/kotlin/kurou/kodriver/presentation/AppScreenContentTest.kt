package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
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

        rule.onNode(hasText("読み上げ")).performClick()
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

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        assertEquals(0, otherReselectedCount)

        rule.onNode(hasText("その他")).performClick()
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

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("読み上げ")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, otherReselectedCount)
    }
}
