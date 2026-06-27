package kurou.kodriver

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
    fun `LMU選択時に読み上げ項目を順にタップする`() {
        selectSimulator("Le Mans Ultimate（Windows版）")

        waitUntilDisplayed("フラッグ")
        clickItemAndNavigateBack("フラッグ")
        clickItem("車両接近")
        clickContentDescription("閾値の説明を表示")
        navigateBack()
        clickItemAndNavigateBack("車両故障")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        selectSimulator("GranTurismo 7（PS5）")

        waitUntilDisplayed("燃料残り周回数")
        clickItemAndNavigateBack("燃料残り周回数")
        clickItemAndNavigateBack("自己ベストラップ")
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        clickItem("その他")
        clickItemAndNavigateBack("接続先PCのIPアドレス")
        clickItemAndNavigateBack("ゲーム機のIPアドレス")
        clickItemAndNavigateBack("音量")
        clickItem("読み上げ開始音")
        clickItem("キャンセル")
        clickItem("ライセンス")
    }

    private fun selectSimulator(simulatorName: String) {
        composeTestRule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        composeTestRule.waitForIdle()
        clickLastItem(simulatorName)
    }

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickItemAndNavigateBack(text: String) {
        clickItem(text)
        navigateBack()
    }

    private fun navigateBack() {
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickItem(text: String) {
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickLastItem(text: String) {
        val nodeIndex = composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().lastIndex
        composeTestRule.onAllNodes(hasText(text)).get(nodeIndex).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickContentDescription(contentDescription: String) {
        composeTestRule.onNode(hasContentDescription(contentDescription)).performClick()
        composeTestRule.waitForIdle()
    }
}
