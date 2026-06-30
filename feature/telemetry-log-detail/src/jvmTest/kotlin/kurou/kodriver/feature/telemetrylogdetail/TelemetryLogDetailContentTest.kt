package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test

class TelemetryLogDetailContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `空の詳細ペインを表示する`() {
        rule.setContent {
            TelemetryLogDetailContent(uiState = TelemetryLogDetailUiState())
        }

        rule.onRoot().assertExists()
    }
}
