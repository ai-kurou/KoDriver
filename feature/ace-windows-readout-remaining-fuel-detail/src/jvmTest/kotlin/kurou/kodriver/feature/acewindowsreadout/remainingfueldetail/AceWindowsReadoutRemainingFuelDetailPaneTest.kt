package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutRemainingFuelDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカード内の閾値項目とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("残り燃料が設定した閾値を下回った場合に、音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("残量閾値").assertIsDisplayed()
        rule.onNodeWithText("残り燃料がこの割合を下回ったら警告を読み上げます。").assertIsDisplayed()
        rule.onNodeWithText("30%").assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定すると表示ラベルが更新される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 30f, range = 5f..90f, steps = 84)),
        ).performSemanticsAction(SemanticsActions.SetProgress) {
            it(60f)
        }

        rule.onNodeWithText("60%").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとデフォルト値に戻る`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 30f, range = 5f..90f, steps = 84)),
        ).performSemanticsAction(SemanticsActions.SetProgress) {
            it(60f)
        }
        rule.onNode(hasContentDescription("デフォルト値にリセット")).performClick()

        rule.onNodeWithText("30%").assertIsDisplayed()
    }
}
