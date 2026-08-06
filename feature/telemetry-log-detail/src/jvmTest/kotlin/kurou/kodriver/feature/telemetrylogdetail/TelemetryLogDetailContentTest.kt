package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class TelemetryLogDetailContentTest {
    @Test
    fun `空の詳細ペインを表示する`() =
        composeScreenshotTest {
            setContent {
                TelemetryLogDetailContent(uiState = TelemetryLogDetailUiState())
            }

            onRoot().assertExists()
        }

    @Test
    fun `2つのテレメトリーデータJSONを表示する`() =
        composeScreenshotTest {
            setContent {
                TelemetryLogDetailContent(
                    uiState =
                        TelemetryLogDetailUiState(
                            logId = 2L,
                            items =
                                listOf(
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

            onNodeWithText("選択したログ").assertExists()
            onNodeWithText("""{"speed":120}""").assertExists()
            onNodeWithText("一つ前のログ").assertExists()
            onNodeWithText("""{"speed":118}""").assertExists()
        }
}
