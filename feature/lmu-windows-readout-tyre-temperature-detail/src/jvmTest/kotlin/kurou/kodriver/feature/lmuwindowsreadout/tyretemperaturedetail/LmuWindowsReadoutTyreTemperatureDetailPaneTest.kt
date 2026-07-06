package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreTemperatureDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `リセットボタンをクリックするとonHighThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 95),
                    onHighThresholdReset = { resetCalled = true },
                )
            }
        }
        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()
        assertEquals(true, resetCalled)
    }

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                )
            }
        }

        rule.onNodeWithContentDescription("閾値の説明を表示").performClick()

        rule.onAllNodesWithText("カーカス温度", substring = true)[0].assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() {
        var changedValue: Int? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
                    onHighThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..100f, steps = 9)),
        ).performSemanticsAction(SemanticsActions.SetProgress) { it(95f) }

        assertEquals(95, changedValue)
    }
}
