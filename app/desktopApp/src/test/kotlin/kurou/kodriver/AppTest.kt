package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.data.desktopDataModule
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
                modules(listOf(desktopDataModule, fakeLmuWindowsNarratorModule, fakeReadoutListModule) + appModules)
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
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動しライセンスを開く`() {
        rule.setContent { AppScreen() }

        // シミュレータ選択ドロップダウンを開く
        rule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        rule.waitForIdle()

        // LMU Windowsシミュレータを選択
        rule.onNodeWithTag("simulator_item_lmu_windows").performClick()
        rule.waitForIdle()

        // 読み上げリストが表示されるまで待機
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodesWithTag("readout_item_0").fetchSemanticsNodes().isNotEmpty()
        }
        // 読み上げ項目0をタップ
        rule.onNodeWithTag("readout_item_0").performClick()
        rule.waitForIdle()

        // 読み上げ項目1をタップ
        rule.onNodeWithTag("readout_item_1").performClick()
        rule.waitForIdle()

        // 読み上げ項目2をタップ
        rule.onNodeWithTag("readout_item_2").performClick()
        rule.waitForIdle()

        // その他タブへ移動
        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()

        // 音量をタップ
        rule.onNodeWithTag("other_item_0").performClick()
        rule.waitForIdle()

        // 読み上げ開始音をタップ（Desktop では ServerIp が含まれないため other_item_1）
        rule.onNodeWithTag("other_item_1").performClick()
        rule.waitForIdle()

        // ダイアログをキャンセル
        rule.onNodeWithText("キャンセル").performClick()
        rule.waitForIdle()

        // ライセンスをタップ（Desktop では ServerIp が含まれないため other_item_4）
        rule.onNodeWithTag("other_item_4").performClick()
        rule.waitForIdle()
    }
}
