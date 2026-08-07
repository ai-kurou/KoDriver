package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class TelemetryLogDetailContentScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            TelemetryLogDetailContent(
                                uiState =
                                    TelemetryLogDetailUiState(
                                        logId = 2L,
                                        items =
                                            listOf(
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
                }
            }

            onRoot().captureRoboImage()
        }
}
