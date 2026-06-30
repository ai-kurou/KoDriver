package kurou.kodriver.feature.telemetryloglist

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class TelemetryLogContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `一覧ペインにログを表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState,
            )
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_PANE_TEST_TAG).assertExists()
        rule.onNodeWithText("flag").assertExists()
        rule.onNodeWithText("vehicle_approach").assertExists()
        rule.onNodeWithText("remaining_fuel_laps").assertExists()
    }
}
