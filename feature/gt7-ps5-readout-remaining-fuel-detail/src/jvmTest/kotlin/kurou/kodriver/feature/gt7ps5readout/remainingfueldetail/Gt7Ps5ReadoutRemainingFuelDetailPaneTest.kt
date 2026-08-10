package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

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
import kotlin.test.assertEquals

class Gt7Ps5ReadoutRemainingFuelDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカード内の閾値項目とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("燃料残量が設定した閾値を下回った場合に、音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("燃料残量警告").assertIsDisplayed()
        rule.onNodeWithText("残量閾値").assertIsDisplayed()
        rule.onNodeWithText("燃料残量が30%以下になると警告を読み上げます").assertIsDisplayed()
        rule.onNodeWithText("30%").assertIsDisplayed()
    }

    @Test
    fun `チップをタップするとonPreviewClickedが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
                    onPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onNodeWithText("燃料残量警告").performClick()

        assertEquals(true, previewClicked)
    }

    @Test
    fun `スライダーの値を確定するとonThresholdChangedが呼ばれる`() {
        var changedPercentage: Int? = null
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
                    onThresholdChanged = { changedPercentage = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 30f, range = 5f..90f, steps = 84)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(60f)
            }

        assertEquals(60, changedPercentage)
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelDetailUiState(thresholdPercentage = 60),
                    onThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルト値にリセット")).performClick()

        assertEquals(true, resetCalled)
    }
}
