package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.window.core.layout.WindowSizeClass
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenContentTest {
    @Test
    fun `expanded幅ではNavigationRailを使用する`() =
        composeScreenshotTest {
            val layoutType =
                WindowSizeClass(
                    minWidthDp = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
                    minHeightDp = 0,
                ).resolveNavigationSuiteType()

            assertEquals(NavigationSuiteType.NavigationRail, layoutType)
        }

    @Test
    fun `medium幅ではNavigationRailを使用する`() =
        composeScreenshotTest {
            val layoutType =
                WindowSizeClass(
                    minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                    minHeightDp = 0,
                ).resolveNavigationSuiteType()

            assertEquals(NavigationSuiteType.NavigationRail, layoutType)
        }

    @Test
    fun `compact幅ではNavigationBarを使用する`() =
        composeScreenshotTest {
            val layoutType =
                WindowSizeClass(
                    minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND - 1,
                    minHeightDp = 0,
                ).resolveNavigationSuiteType()

            assertEquals(NavigationSuiteType.NavigationBar, layoutType)
        }

    @Test
    fun `読み上げタブを再タップするとonReadoutTabReselectedが呼ばれる`() =
        composeScreenshotTest {
            var readoutReselectedCount = 0
            var logReselectedCount = 0
            var otherReselectedCount = 0

            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    onReadoutTabReselected = { readoutReselectedCount++ },
                    onLogTabReselected = { logReselectedCount++ },
                    onOtherTabReselected = { otherReselectedCount++ },
                )
            }

            onNode(hasText("読み上げ")).performClick()
            waitForIdle()

            assertEquals(1, readoutReselectedCount)
            assertEquals(0, logReselectedCount)
            assertEquals(0, otherReselectedCount)
        }

    @Test
    fun `ログタブを再タップするとonLogTabReselectedが呼ばれる`() =
        composeScreenshotTest {
            var readoutReselectedCount = 0
            var logReselectedCount = 0
            var otherReselectedCount = 0

            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    onReadoutTabReselected = { readoutReselectedCount++ },
                    onLogTabReselected = { logReselectedCount++ },
                    onOtherTabReselected = { otherReselectedCount++ },
                )
            }

            onNode(hasText("ログ")).performClick()
            waitForIdle()
            assertEquals(0, logReselectedCount)

            onNode(hasText("ログ")).performClick()
            waitForIdle()

            assertEquals(0, readoutReselectedCount)
            assertEquals(1, logReselectedCount)
            assertEquals(0, otherReselectedCount)
        }

    @Test
    fun `その他タブを再タップするとonOtherTabReselectedが呼ばれる`() =
        composeScreenshotTest {
            var readoutReselectedCount = 0
            var logReselectedCount = 0
            var otherReselectedCount = 0

            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    onReadoutTabReselected = { readoutReselectedCount++ },
                    onLogTabReselected = { logReselectedCount++ },
                    onOtherTabReselected = { otherReselectedCount++ },
                )
            }

            onNode(hasText("その他")).performClick()
            waitForIdle()
            assertEquals(0, otherReselectedCount)

            onNode(hasText("その他")).performClick()
            waitForIdle()

            assertEquals(0, readoutReselectedCount)
            assertEquals(0, logReselectedCount)
            assertEquals(1, otherReselectedCount)
        }

    @Test
    fun `別タブに切り替えてもreselectedコールバックは呼ばれない`() =
        composeScreenshotTest {
            var readoutReselectedCount = 0
            var logReselectedCount = 0
            var otherReselectedCount = 0

            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    onReadoutTabReselected = { readoutReselectedCount++ },
                    onLogTabReselected = { logReselectedCount++ },
                    onOtherTabReselected = { otherReselectedCount++ },
                )
            }

            onNode(hasText("その他")).performClick()
            waitForIdle()
            onNode(hasText("読み上げ")).performClick()
            waitForIdle()

            assertEquals(0, readoutReselectedCount)
            assertEquals(0, logReselectedCount)
            assertEquals(0, otherReselectedCount)
        }

    @Test
    fun `ログタブを選択するとtelemetryLogContentが表示される`() =
        composeScreenshotTest {
            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    telemetryLogContent = { _ -> Text("TelemetryLogContent") },
                )
            }

            onNode(hasText("ログ")).performClick()
            waitForIdle()

            onNodeWithText("TelemetryLogContent").assertExists()
        }

    @Test
    fun `dynamicColorEnabledがtrueでもJVMではフォールバックのテーマで描画される`() =
        composeScreenshotTest {
            setContent {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    dynamicColorEnabled = true,
                )
            }

            onNode(hasText("その他")).assertExists()
        }
}
