package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppScreenManualReadoutTabTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `NavigationBarの手動読み上げタブは自動読み上げとログの間に表示され選択と再選択で手動画面を表示する`() {
        assertManualReadoutTab(NavigationSuiteType.NavigationBar)
    }

    @Test
    fun `NavigationRailの手動読み上げタブは自動読み上げとログの間に表示され選択と再選択で手動画面を表示する`() {
        assertManualReadoutTab(NavigationSuiteType.NavigationRail)
    }

    private fun assertManualReadoutTab(layoutType: NavigationSuiteType) {
        var reselectedCount = 0
        var manualReselectedCount = 0
        rule.setContent {
            AppScreenContent(
                layoutType = layoutType,
                onManualReadoutTabReselected = { manualReselectedCount++ },
                manualReadoutTabReselectRequest = 7,
                onAutoReadoutTabReselected = { reselectedCount++ },
                onLogTabReselected = { reselectedCount++ },
                onOtherTabReselected = { reselectedCount++ },
                autoReadoutContent = { Text("読み上げ画面") },
                manualReadoutContent = { _, request ->
                    assertEquals(7, request)
                    Text("手動画面")
                },
                telemetryLogContent = { _, _ -> Text("ログ画面") },
                otherContent = { _, _ -> Text("その他画面") },
            )
        }

        val labels = listOf("自動", "手動", "ログ", "その他")
        labels.forEach { rule.onNodeWithText(it).assertIsDisplayed() }
        val bounds = labels.map { rule.onNodeWithText(it).fetchSemanticsNode().boundsInRoot }
        bounds.zipWithNext().forEach { (previous, next) ->
            if (layoutType == NavigationSuiteType.NavigationBar) {
                assertTrue(previous.center.x < next.center.x)
            } else {
                assertTrue(previous.center.y < next.center.y)
            }
        }

        listOf(
            "自動" to "読み上げ画面",
            "ログ" to "ログ画面",
            "その他" to "その他画面",
        ).forEach { (label, content) ->
            if (label != "自動") {
                rule.onNodeWithText(label).performClick()
                rule.waitForIdle()
            }
            repeat(2) {
                rule.onNodeWithText("手動").performClick()
                rule.waitForIdle()
                rule.onNodeWithText("手動").assertIsSelected()
                rule.onNodeWithText(label).assertIsNotSelected()
                rule.onNodeWithText(content).assertDoesNotExist()
                rule.onNodeWithText("手動画面").assertIsDisplayed()
            }
        }
        assertEquals(0, reselectedCount)
        assertEquals(3, manualReselectedCount)
    }
}
