package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreWearDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        rule.onNodeWithText(
            "タイヤの摩耗率が設定した閾値以上になった場合に音声でお知らせします。" +
                "いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値未満に戻るまでは再度読み上げません。",
        ).assertIsDisplayed()
    }

    @Test
    fun `摩耗警告カードとデフォルトONのタイヤ摩耗警告チップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        rule.onNodeWithText("摩耗警告").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗警告")
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun `タイヤ摩耗警告チップをタップするとonWarningChipClickedが呼ばれる`() {
        var clicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent(
                    onWarningChipClicked = { clicked = true },
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗警告").performClick()

        assertEquals(true, clicked)
    }

    @Test
    fun `摩耗閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        rule.onNodeWithText("摩耗閾値").assertIsDisplayed()
        rule.onNodeWithText("この閾値になると警告を読み上げます").assertIsDisplayed()
        rule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreWearDetailUiState(thresholdPercentage = 30),
                    onThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()

        assertEquals(true, resetCalled)
    }
}
