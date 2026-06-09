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
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動する`() {
        // シミュレータドロップダウンをタップして展開
        composeTestRule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        composeTestRule.waitForIdle()

        // Le Mans Ultimate を選択
        composeTestRule.onNodeWithTag("simulator_item_lmu").performClick()
        composeTestRule.waitForIdle()

        // 読み上げ優先度リストの最上部（インデックス0）をタップ
        composeTestRule.onNodeWithTag("readout_item_0").performClick()
        composeTestRule.waitForIdle()

        // はてなマークをタップ
        composeTestRule.onNodeWithTag("vehicle_approach_help_button").performClick()
        composeTestRule.waitForIdle()

        // その他タブをタップ
        composeTestRule.onNodeWithTag("nav_more").performClick()
        composeTestRule.waitForIdle()

        // ライセンス項目をタップ
        composeTestRule.onNodeWithTag("other_item_0").performClick()
        composeTestRule.waitForIdle()
    }
}
