package kurou.kodriver.feature.manualreadoutlist

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

class SpeechRecognitionCardScreenshotTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @Test
    fun `一覧の音声認識_停止中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState = ManualReadoutListUiState(),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                                speechRecognitionSection = {
                                    SpeechRecognitionCard(
                                        uiState = SpeechRecognitionUiState(),
                                        onEnabledChange = {},
                                    )
                                },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `一覧の音声認識_聞き取り中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState = ManualReadoutListUiState(),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                                speechRecognitionSection = {
                                    SpeechRecognitionCard(
                                        uiState =
                                            SpeechRecognitionUiState(
                                                enabled = true,
                                                status = SpeechRecognitionStatus.Listening,
                                                partialText = "タイヤの",
                                                recognized =
                                                    listOf(
                                                        listOf("燃料残量", "燃料残り"),
                                                        listOf("ブレーキの温度"),
                                                    ),
                                            ),
                                        onEnabledChange = {},
                                    )
                                },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `一覧の音声認識_権限未許可`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState = ManualReadoutListUiState(),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                                speechRecognitionSection = {
                                    SpeechRecognitionCard(
                                        uiState =
                                            SpeechRecognitionUiState(
                                                status = SpeechRecognitionStatus.PermissionDenied,
                                            ),
                                        onEnabledChange = {},
                                    )
                                },
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
