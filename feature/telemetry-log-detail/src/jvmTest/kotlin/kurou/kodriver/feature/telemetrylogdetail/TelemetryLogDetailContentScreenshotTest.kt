package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class TelemetryLogDetailContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                TelemetryLogDetailContent(
                    uiState = TelemetryLogDetailUiState(
                        logId = 2L,
                        items = listOf(
                            TelemetryLogDetailItemUiState(
                                title = "選択したログ",
                                telemetryJson = """{"speed":120,"gear":4}""",
                            ),
                            TelemetryLogDetailItemUiState(
                                title = "一つ前のログ",
                                telemetryJson = """{"speed":118,"gear":4}""",
                            ),
                        ),
                    ),
                )
            }
        }

        rule.onRoot().captureRoboImage()
    }
}
