package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト値3周のスライダーと説明を表示する`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent()
            }
        }

        rule.onNodeWithText("現在の最速ラップの30秒前にあたるタイミングで判定し", substring = true)
            .assertIsDisplayed()
        rule.onNodeWithText("閾値設定").assertIsDisplayed()
        rule.onNodeWithText("残り約: 3 周").assertIsDisplayed()
    }

    @Test
    fun `スライダーに1周を表示できる`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 1),
                    onRemainingFuelLapsChanged = {},
                )
            }
        }

        rule.onNodeWithText("残り約: 1 周").assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定するとonRemainingFuelLapsChangedが呼ばれる`() {
        var changedRemainingFuelLaps: Int? = null
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 3),
                    onRemainingFuelLapsChanged = { changedRemainingFuelLaps = it },
                )
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
        ).performSemanticsAction(SemanticsActions.SetProgress) {
            it(5f)
        }

        assertEquals(5, changedRemainingFuelLaps)
    }

    @Test
    fun `リセットボタンをタップするとonResetRemainingFuelLapsが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 5),
                    onRemainingFuelLapsChanged = {},
                    onResetRemainingFuelLaps = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルト値にリセット")).performClick()

        assertTrue(resetCalled)
    }
}
