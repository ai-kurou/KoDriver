package kurou.kodriver

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
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
        clickReadoutPriorityHelp()

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
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残り周回数")
        clickItemAndNavigateBack("燃料残り周回数")
        clickItemAndNavigateBack("自己ベストラップ")
    }

    @Test
    fun `LMU選択時に接続状況バナーをタップして戻る`() {
        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("Windows版KoDriverへ接続するIPアドレスが未設定です")
        clickItem("Windows版KoDriverへ接続するIPアドレスが未設定です")
        navigateBack()
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        selectSimulator("GranTurismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        navigateBack()
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        clickItem("その他")
        clickItemAndNavigateBack("Windows版KoDriverへ接続するIPアドレス")
        clickItemAndNavigateBack("ゲーム機・SimHubへ接続するIPアドレス")
        clickItemAndNavigateBack("音量")
        clickItem("画面をスリープさせない")
        clickItem("キャンセル")
        clickItem("読み上げ開始音")
        clickItem("キャンセル")
        clickItemAndNavigateBack("ライセンス")
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
        if (composeTestRule.onAllNodes(hasContentDescription("戻る")).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        } else {
            composeTestRule.runOnIdle {
                composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
            }
        }
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

    private fun clickReadoutPriorityHelp() {
        clickContentDescription(READOUT_PRIORITY_HELP_DESCRIPTION)
        // 実機では外側タップでボトムシートが閉じないことがあるため、dismissアクションを直接実行する。
        composeTestRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .get(0)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(READOUT_PRIORITY_HELP_DESCRIPTION)).fetchSemanticsNodes().isEmpty()
        }
    }

    private companion object {
        const val READOUT_PRIORITY_HELP_DESCRIPTION =
            "上位の項目は読み上げ中でも割り込みます。読み上げ中の同順位・下位の項目は無視されます"
    }
}
