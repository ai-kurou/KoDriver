package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import org.junit.Test

class Gt7Ps5DebugStateDetailPaneScreenshotTest {
    @Test
    fun `デフォルト データ未取得`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                            Gt7Ps5DebugStateDetailPaneContent(
                                uiState = Gt7Ps5DebugStateDetailUiState(),
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `全カードにデータ取得済み`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                            Gt7Ps5DebugStateDetailPaneContent(
                                uiState =
                                    Gt7Ps5DebugStateDetailUiState(
                                        enabledCardKeys = gt7Ps5SupportedCardKeys,
                                        gt7Ps5 =
                                            Gt7Ps5DebugState(
                                                telemetry = sampleGt7Ps5Telemetry(),
                                                vehicleClass = Gt7Ps5VehicleClassData("GR3"),
                                            ),
                                    ),
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
