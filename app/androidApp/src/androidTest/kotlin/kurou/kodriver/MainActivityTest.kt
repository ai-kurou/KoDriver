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
    fun `シミュレータ選択後に読み上げ項目を順にタップしその他タブへ移動しライセンスを開く`() {
        // シミュレータ選択ドロップダウンを開く
        composeTestRule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        composeTestRule.waitForIdle()

        // LMU Windowsシミュレータを選択
        composeTestRule.onNode(hasText("Le Mans Ultimate（Windows版）")).performClick()
        composeTestRule.waitForIdle()

        // 読み上げリストが表示されるまで待機
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText("フラッグ")).fetchSemanticsNodes().isNotEmpty()
        }
        // 読み上げ項目「フラッグ」をタップ
        composeTestRule.onNode(hasText("フラッグ")).performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // 読み上げ項目「車両接近」をタップ
        composeTestRule.onNode(hasText("車両接近")).performClick()
        composeTestRule.waitForIdle()

        // はてなマークをタップ
        composeTestRule.onNode(hasContentDescription("閾値の説明を表示")).performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // 読み上げ項目「車両故障」をタップ
        composeTestRule.onNode(hasText("車両故障")).performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // その他タブへ移動
        composeTestRule.onNode(hasText("その他")).performClick()
        composeTestRule.waitForIdle()

        // 接続先PCをタップ
        composeTestRule.onNode(hasText("接続先PCのIPアドレス")).performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // コンソールIPをタップ
        composeTestRule.onNode(hasText("ゲーム機のIPアドレス")).performClick()
        composeTestRule.waitForIdle()

        // 詳細ペインから戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // 音量をタップ
        composeTestRule.onNode(hasText("音量")).performClick()
        composeTestRule.waitForIdle()

        // 音量詳細から戻る
        composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        composeTestRule.waitForIdle()

        // 読み上げ開始音をタップ
        composeTestRule.onNode(hasText("読み上げ開始音")).performClick()
        composeTestRule.waitForIdle()

        // ダイアログをキャンセル
        composeTestRule.onNodeWithText("キャンセル").performClick()
        composeTestRule.waitForIdle()

        // ライセンスをタップ
        composeTestRule.onNode(hasText("ライセンス")).performClick()
        composeTestRule.waitForIdle()
    }
}
