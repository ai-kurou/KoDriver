package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    @Test
    fun `2つのテレメトリーデータJSONを表示する`() {
        rule.setContent {
            TelemetryLogDetailContent(
                uiState = TelemetryLogDetailUiState(
                    logId = 2L,
                    items = listOf(
                        TelemetryLogDetailItemUiState(
                            title = "選択したログ",
                            telemetryJson = """{"speed":120}""",
                        ),
                        TelemetryLogDetailItemUiState(
                            title = "一つ前のログ",
                            telemetryJson = """{"speed":118}""",
                        ),
                    ),
                ),
            )
        }

        rule.onNodeWithText("選択したログ").assertExists()
        rule.onNodeWithText("""{"speed":120}""").assertExists()
        rule.onNodeWithText("一つ前のログ").assertExists()
        rule.onNodeWithText("""{"speed":118}""").assertExists()
    }
}
