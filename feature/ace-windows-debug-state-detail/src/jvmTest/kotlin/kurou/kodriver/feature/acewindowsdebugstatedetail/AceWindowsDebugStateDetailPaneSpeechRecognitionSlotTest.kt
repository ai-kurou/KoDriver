package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class AceWindowsDebugStateDetailPaneSpeechRecognitionSlotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `渡した音声認識のカードを読み上げカードより上に表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                    modifier = Modifier.requiredSize(360.dp, 600.dp),
                    speechRecognitionSection = { modifier -> Text(SLOT_TEXT, modifier) },
                )
            }
        }

        rule.onNodeWithText(SLOT_TEXT).assertIsDisplayed()
        rule.onNodeWithTag("debug_readout_request_card").assertIsDisplayed()
        val slotBounds = rule.onNodeWithText(SLOT_TEXT).fetchSemanticsNode().boundsInRoot
        val readoutBounds = rule.onNodeWithTag("debug_readout_request_card").fetchSemanticsNode().boundsInRoot
        assertTrue(slotBounds.top < readoutBounds.top)
    }

    @Test
    fun `音声認識のカードを渡さなくても読み上げカードを表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                    modifier = Modifier.requiredSize(360.dp, 600.dp),
                )
            }
        }

        rule.onNodeWithText(SLOT_TEXT).assertDoesNotExist()
        rule.onNodeWithTag("debug_readout_request_card").assertIsDisplayed()
    }

    private companion object {
        const val SLOT_TEXT = "音声認識スロット"
    }
}
