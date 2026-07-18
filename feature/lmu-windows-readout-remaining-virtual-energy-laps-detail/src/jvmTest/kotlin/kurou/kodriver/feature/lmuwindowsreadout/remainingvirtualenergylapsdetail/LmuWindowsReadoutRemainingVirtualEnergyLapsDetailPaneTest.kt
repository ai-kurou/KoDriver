package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

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

class LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト値3周のスライダーと説明を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent()
            }
        }

        rule.onNodeWithText("バーチャルエナジー残量から走行可能な残り周回数を計算し", substring = true)
            .assertIsDisplayed()
        rule.onNodeWithText("残り約: 3 周").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー残り周回数").assertIsDisplayed()
    }

    @Test
    fun `スライダーに1周を表示できる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(
                        remainingVirtualEnergyLaps = 1,
                    ),
                )
            }
        }

        rule.onNodeWithText("残り約: 1 周").assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定するとonRemainingVirtualEnergyLapsChangedが呼ばれる`() {
        var changedRemainingVirtualEnergyLaps: Int? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(
                        remainingVirtualEnergyLaps = 3,
                    ),
                    onRemainingVirtualEnergyLapsChanged = { changedRemainingVirtualEnergyLaps = it },
                )
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
        ).performSemanticsAction(SemanticsActions.SetProgress) {
            it(5f)
        }

        assertEquals(5, changedRemainingVirtualEnergyLaps)
    }

    @Test
    fun `リセットボタンをタップするとonResetRemainingVirtualEnergyLapsが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(
                        remainingVirtualEnergyLaps = 5,
                    ),
                    onRemainingVirtualEnergyLapsChanged = {},
                    onResetRemainingVirtualEnergyLaps = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルト値にリセット")).performClick()

        assertTrue(resetCalled)
    }
}
