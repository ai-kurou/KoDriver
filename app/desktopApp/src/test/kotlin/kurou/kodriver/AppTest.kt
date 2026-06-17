package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class AppTest {

    companion object {
        @BeforeClass @JvmStatic
        fun setUpKoin() {
            startKoin {
                modules(listOf(fakeLmuWindowsNarratorModule, fakeReadoutListModule) + appModules)
            }
        }

        @AfterClass @JvmStatic
        fun tearDownKoin() {
            stopKoin()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `アプリ画面が起動する`() {
        rule.setContent {
            AppScreen(readoutContent = {})
        }
        rule.waitForIdle()
    }

    @Test
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動する`() {
        rule.setContent { AppScreen() }

        rule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("simulator_item_lmu_windows").performClick()
        rule.waitForIdle()

        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodesWithTag("readout_item_0").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("readout_item_0").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("readout_item_1").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("readout_item_2").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("other_item_0").performClick()
        rule.waitForIdle()
    }
}
