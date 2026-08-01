package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
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

        rule.onNodeWithContentDescription("高温閾値の説明を表示").performClick()

        rule.onNodeWithText("設定した温度以上になると過熱警告を読み上げます", substring = true).assertIsDisplayed()
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

        rule
            .onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..100f, steps = 9)),
        ).performSemanticsAction(SemanticsActions.SetProgress) { it(95f) }

        assertEquals(95, changedValue)
    }

    @Test
    fun `過熱警告カードのヘッダーをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `タイヤ過熱警告チップをタップするとonPreviewClickedが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                    onPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onAllNodesWithText("タイヤ過熱警告", substring = true)[0].performClick()

        assertEquals(true, previewClicked)
    }

    @Test
    fun `タイヤ低温警告チップをタップするとonLowWarningPreviewClickedが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                    onLowWarningPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].performClick()

        assertEquals(true, previewClicked)
    }

    @Test
    fun `タイヤ低温警告チップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(lowWarningEnabled = true),
                )
            }
        }

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].assertIsDisplayed()
    }
}
