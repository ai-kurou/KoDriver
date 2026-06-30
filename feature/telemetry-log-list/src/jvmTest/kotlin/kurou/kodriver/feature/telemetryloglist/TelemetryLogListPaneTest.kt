package kurou.kodriver.feature.telemetryloglist

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class TelemetryLogListPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `空の一覧を表示する`() {
        rule.setContent {
            TelemetryLogListPane()
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_PANE_TEST_TAG).assertExists()
    }

    @Test
    fun `TelemetryLogContentScaffoldで一覧ペインを表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold()
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_PANE_TEST_TAG).assertExists()
    }
}
