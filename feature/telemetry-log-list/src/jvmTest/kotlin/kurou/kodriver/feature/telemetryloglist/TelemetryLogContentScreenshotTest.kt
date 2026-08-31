package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.buildlogic.screenshottest.singlePaneDirective
import kurou.kodriver.buildlogic.screenshottest.twoPaneDirective
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class TelemetryLogContentScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            TelemetryLogContentScaffold(
                                uiState = previewTelemetryLogListUiState.copy(selectedLogId = 2),
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { logId -> Text("Detail: $logId") },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `一覧のみ表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            TelemetryLogContentScaffold(
                                uiState = previewTelemetryLogListUiState.copy(selectedLogId = null),
                                scaffoldDirective = singlePaneDirective,
                                detailContent = { logId -> Text("Detail: $logId") },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }
}
