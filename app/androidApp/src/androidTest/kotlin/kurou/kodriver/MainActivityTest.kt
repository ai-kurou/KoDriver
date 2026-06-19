package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `シミュレータ選択後に読み上げ項目を順にタップしその他タブへ移動しライセンスを開く`() {
        // シミュレータ選択ドロップダウンを開く
        composeTestRule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        composeTestRule.waitForIdle()

        // LMU Windowsシミュレータを選択
        composeTestRule.onNodeWithTag("simulator_item_lmu_windows").performClick()
        composeTestRule.waitForIdle()

        // 読み上げリストが表示されるまで待機
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithTag("readout_item_0").fetchSemanticsNodes().isNotEmpty()
        }
        // 読み上げ項目0をタップ
        composeTestRule.onNodeWithTag("readout_item_0").performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNodeWithTag("readout_detail_back").performClick()
        composeTestRule.waitForIdle()

        // 読み上げ項目1をタップ
        composeTestRule.onNodeWithTag("readout_item_1").performClick()
        composeTestRule.waitForIdle()

        // はてなマークをタップ
        composeTestRule.onNodeWithTag("vehicle_approach_help_button").performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNodeWithTag("readout_detail_back").performClick()
        composeTestRule.waitForIdle()

        // 読み上げ項目2をタップ
        composeTestRule.onNodeWithTag("readout_item_2").performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNodeWithTag("readout_detail_back").performClick()
        composeTestRule.waitForIdle()

        // その他タブへ移動
        composeTestRule.onNodeWithTag("nav_more").performClick()
        composeTestRule.waitForIdle()

        // 接続先サーバーをタップ（AndroidではServerIpが含まれるためother_item_0）
        composeTestRule.onNodeWithTag("other_item_0").performClick()
        composeTestRule.waitForIdle()

        // ダイアログをキャンセル
        composeTestRule.onNodeWithText("キャンセル").performClick()
        composeTestRule.waitForIdle()

        // 音量をタップ（AndroidではServerIpが含まれるためother_item_1）
        composeTestRule.onNodeWithTag("other_item_1").performClick()
        composeTestRule.waitForIdle()

        // 音量詳細から戻る（Androidではシングルペインのためリスト非表示になる）
        composeTestRule.onNodeWithTag("other_detail_back").performClick()
        composeTestRule.waitForIdle()

        // 読み上げ開始音をタップ（AndroidではServerIpが含まれるためother_item_2）
        composeTestRule.onNodeWithTag("other_item_2").performClick()
        composeTestRule.waitForIdle()

        // ダイアログをキャンセル
        composeTestRule.onNodeWithText("キャンセル").performClick()
        composeTestRule.waitForIdle()

        // ライセンスをタップ（AndroidではServerIpが含まれるためother_item_5）
        composeTestRule.onNodeWithTag("other_item_5").performClick()
        composeTestRule.waitForIdle()
    }
}
