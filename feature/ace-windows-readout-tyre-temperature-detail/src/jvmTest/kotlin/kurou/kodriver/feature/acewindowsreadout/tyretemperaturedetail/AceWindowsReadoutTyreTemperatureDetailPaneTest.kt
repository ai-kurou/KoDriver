package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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

class AceWindowsReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤ温度").assertIsDisplayed()
        rule
            .onNodeWithText(
                "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、" +
                    "ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
            ).assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのタイトルとチップと高温閾値設定が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("過熱警告").assertIsDisplayed()
        rule.onNodeWithText("タイヤ過熱警告").assertIsDisplayed()
        rule.onNodeWithText("高温閾値設定").assertIsDisplayed()
        rule.onNodeWithText("高温閾値: 90°C").assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのスイッチをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `プレビューチップをタップするとonPreviewClickedが呼ばれる`() {
        var previewCount = 0
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onPreviewClicked = { previewCount++ },
                )
            }
        }

        rule.onNodeWithText("タイヤ過熱警告").assertIsEnabled().performClick()

        assertEquals(1, previewCount)
    }

    @Test
    fun `過熱警告が無効ならプレビューチップも無効になる`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = false),
                )
            }
        }

        rule.onNodeWithText("タイヤ過熱警告").assertIsNotEnabled()
    }

    @Test
    fun `高温閾値スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() {
        var changedCelsius: Int? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    onHighThresholdChanged = { changedCelsius = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..110f, steps = 19)),
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
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 105),
                    onHighThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }
}
