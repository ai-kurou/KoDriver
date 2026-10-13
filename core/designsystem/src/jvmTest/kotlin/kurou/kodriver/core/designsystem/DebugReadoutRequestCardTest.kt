package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugReadoutRequestCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `項目を選択して入力した文言を保持し空白のみでは送信できない`() {
        var selected = ""
        var sent = ""
        rule.setContent {
            KoDriverTheme {
                var key by remember { mutableStateOf("gt7_ps5_auto_my_best_lap") }
                var text by remember { mutableStateOf("") }
                DebugReadoutRequestCard(
                    readoutItemKeyValues = listOf("gt7_ps5_auto_my_best_lap", "gt7_ps5_auto_remaining_fuel"),
                    selectedReadoutItemKeyValue = key,
                    text = text,
                    onReadoutItemSelected = {
                        key = it
                        selected = it
                    },
                    onTextChange = { text = it },
                    onReadout = { sent = text },
                )
            }
        }
        rule.onNodeWithText("読み上げ").assertIsNotEnabled()
        rule.onNodeWithTag("debug_readout_request_item").performClick()
        rule.onNodeWithText("燃料残量").performClick()
        assertEquals("gt7_ps5_auto_remaining_fuel", selected)
        rule.onNodeWithTag("debug_readout_request_text").performTextReplacement(" ")
        rule.onNodeWithText("読み上げ").assertIsNotEnabled()
        rule.onNodeWithTag("debug_readout_request_text").performTextReplacement("入力 {laps}")
        rule.onNodeWithText("読み上げ").assertIsEnabled().performClick()
        assertEquals("入力 {laps}", sent)
        rule.onNodeWithText("入力 {laps}").assertIsDisplayed()
    }
}
