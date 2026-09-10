package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRainDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutRainDetailPaneContent()
            }
        }

        rule.onNodeWithText("降雨の開始・終了を音声でお知らせします。").assertIsDisplayed()
    }

    @Test
    fun `降り始めの読み上げカードが表示される`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutRainDetailPaneContent()
            }
        }

        rule.onNodeWithText("降り始めの読み上げ").assertIsDisplayed()
    }

    @Test
    fun `降り始めの読み上げスイッチをタップするとonStartReadoutEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutRainDetailPaneContent(
                    uiState = LmuWindowsReadoutRainDetailUiState(startReadoutEnabled = true),
                    onStartReadoutEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNode(hasText("降り始めの読み上げ")).performClick()

        assertEquals(false, changedEnabled)
    }
}
