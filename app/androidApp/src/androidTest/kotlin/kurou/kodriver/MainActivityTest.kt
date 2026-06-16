package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
    fun `シミュレータ選択後に読み上げ項目を順にタップしその他タブへ移動する`() {
        // シミュレータドロップダウンをタップして展開
        composeTestRule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        composeTestRule.waitForIdle()

        // Le Mans Ultimate を選択
        composeTestRule.onNodeWithTag("simulator_item_lmu_windows").performClick()
        composeTestRule.waitForIdle()

        // フラッグ（インデックス0）をタップ
        composeTestRule.onNodeWithTag("readout_item_0").performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNodeWithTag("readout_detail_back").performClick()
        composeTestRule.waitForIdle()

        // 車両接近（インデックス1）をタップ
        composeTestRule.onNodeWithTag("readout_item_1").performClick()
        composeTestRule.waitForIdle()

        // はてなマークをタップ
        composeTestRule.onNodeWithTag("vehicle_approach_help_button").performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNodeWithTag("readout_detail_back").performClick()
        composeTestRule.waitForIdle()

        // 車両故障（インデックス2）をタップ
        composeTestRule.onNodeWithTag("readout_item_2").performClick()
        composeTestRule.waitForIdle()

        // その他タブをタップ
        composeTestRule.onNodeWithTag("nav_more").performClick()
        composeTestRule.waitForIdle()

        // ライセンス項目をタップ
        composeTestRule.onNodeWithTag("other_item_3").performClick()
        composeTestRule.waitForIdle()
    }
}
