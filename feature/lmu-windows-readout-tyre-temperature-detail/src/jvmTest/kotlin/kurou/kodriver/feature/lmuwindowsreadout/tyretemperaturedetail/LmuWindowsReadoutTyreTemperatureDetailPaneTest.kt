package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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

        rule.onNode(hasText("カーカス温度", substring = true)).assertIsDisplayed()
    }
}
