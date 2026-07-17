package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.window.core.layout.WindowSizeClass
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `expanded幅ではNavigationRailを使用する`() {
        val layoutType = WindowSizeClass(
            minWidthDp = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
            minHeightDp = 0,
        ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationRail, layoutType)
    }

    @Test
    fun `medium幅ではNavigationRailを使用する`() {
        val layoutType = WindowSizeClass(
            minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
            minHeightDp = 0,
        ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationRail, layoutType)
    }

    @Test
    fun `compact幅ではNavigationBarを使用する`() {
        val layoutType = WindowSizeClass(
            minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND - 1,
            minHeightDp = 0,
        ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationBar, layoutType)
    }

    @Test
    fun `読み上げタブを再タップするとonReadoutTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("読み上げ")).performClick()
        rule.waitForIdle()

        assertEquals(1, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `ログタブを再タップするとonLogTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()
        assertEquals(0, logReselectedCount)

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(1, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `その他タブを再タップするとonOtherTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        assertEquals(0, otherReselectedCount)

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(1, otherReselectedCount)
    }

    @Test
    fun `別タブに切り替えてもreselectedコールバックは呼ばれない`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("読み上げ")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `ログタブを選択するとtelemetryLogContentが表示される`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                telemetryLogContent = { Text("TelemetryLogContent") },
            )
        }

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("TelemetryLogContent").assertExists()
    }

    @Test
    fun `dynamicColorEnabledがtrueでもJVMではフォールバックのテーマで描画される`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                dynamicColorEnabled = true,
            )
        }

        rule.onNode(hasText("その他")).assertExists()
    }
}
