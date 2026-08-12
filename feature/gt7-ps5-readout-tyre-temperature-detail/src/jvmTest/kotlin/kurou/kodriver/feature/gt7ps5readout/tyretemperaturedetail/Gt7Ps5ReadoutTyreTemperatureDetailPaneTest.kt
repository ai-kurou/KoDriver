package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

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

class Gt7Ps5ReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカードタイトルとデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤの温度状況を音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("過熱警告").assertIsDisplayed()
        rule.onNodeWithText("高温閾値設定").assertIsDisplayed()
        rule.onNodeWithText("高温閾値: 95°C").assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのスイッチをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
                    uiState = Gt7Ps5ReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `高温閾値スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() {
        var changedCelsius: Int? = null
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
                    onHighThresholdChanged = { changedCelsius = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 95f, range = 90f..110f, steps = 19)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(105f)
            }

        assertEquals(105, changedCelsius)
    }

    @Test
    fun `デフォルト値から変更している場合に高温閾値のリセットボタンをタップするとonHighThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
                    uiState = Gt7Ps5ReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 105),
                    onHighThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }
}
