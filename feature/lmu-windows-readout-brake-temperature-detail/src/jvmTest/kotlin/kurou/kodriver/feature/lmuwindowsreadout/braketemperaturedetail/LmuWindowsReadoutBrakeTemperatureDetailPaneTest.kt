package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

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

class LmuWindowsReadoutBrakeTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("ブレーキ温度が過熱した際に音声でお知らせします。").assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードとデフォルトONのブレーキ過熱警告チップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("過熱警告").assertIsDisplayed()
        rule
            .onNodeWithText("ブレーキ過熱警告")
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun `ブレーキ過熱警告チップをタップするとonWarningChipClickedが呼ばれる`() {
        var clicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
                    onWarningChipClicked = { clicked = true },
                )
            }
        }

        rule.onNodeWithText("ブレーキ過熱警告").performClick()

        assertEquals(true, clicked)
    }

    @Test
    fun `過熱閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("過熱閾値").assertIsDisplayed()
        rule.onNodeWithText("いずれかのブレーキが700℃以上になると警告を読み上げます。").assertIsDisplayed()
        rule.onNodeWithText("700℃").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeTemperatureDetailUiState(highThresholdCelsius = 600),
                    onThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()

        assertEquals(true, resetCalled)
    }
}
