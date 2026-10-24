package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AndroidAudioUsage
import org.junit.Test

class OtherAndroidAudioUsageDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherAndroidAudioUsageDetailPaneContent(
                                uiState = OtherAndroidAudioUsageDetailUiState(),
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
    fun `試聴中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherAndroidAudioUsageDetailPaneContent(
                                uiState =
                                    OtherAndroidAudioUsageDetailUiState(
                                        previewingUsage = AndroidAudioUsage.ALARM,
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `音量ボタンあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherAndroidAudioUsageDetailPaneContent(
                                uiState = OtherAndroidAudioUsageDetailUiState(),
                                onOpenVolumePanel = {},
                                onOpenSoundSettings = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
