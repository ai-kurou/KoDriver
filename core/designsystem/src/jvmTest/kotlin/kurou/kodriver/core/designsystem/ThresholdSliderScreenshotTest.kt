package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class ThresholdSliderScreenshotTest {
    @Test
    fun `デフォルト値と一致`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 100.dp).padding(16.dp)) {
                            ThresholdSlider(
                                value = 50f,
                                valueRange = 0f..100f,
                                labelFormatter = { "${it.toInt()}%" },
                                onValueChangeFinished = {},
                                defaultValue = 50f,
                                onResetToDefault = {},
                                resetContentDescription = "デフォルトに戻す",
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `デフォルト値と異なる`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 100.dp).padding(16.dp)) {
                            ThresholdSlider(
                                value = 80f,
                                valueRange = 0f..100f,
                                labelFormatter = { "${it.toInt()}%" },
                                onValueChangeFinished = {},
                                defaultValue = 50f,
                                onResetToDefault = {},
                                resetContentDescription = "デフォルトに戻す",
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `デフォルト値と異なる ダークテーマ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = true) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 100.dp).padding(16.dp)) {
                            ThresholdSlider(
                                value = 80f,
                                valueRange = 0f..100f,
                                labelFormatter = { "${it.toInt()}%" },
                                onValueChangeFinished = {},
                                defaultValue = 50f,
                                onResetToDefault = {},
                                resetContentDescription = "デフォルトに戻す",
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `リセットボタンなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 100.dp).padding(16.dp)) {
                            ThresholdSlider(
                                value = 50f,
                                valueRange = 0f..100f,
                                labelFormatter = { "${it.toInt()}%" },
                                onValueChangeFinished = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
