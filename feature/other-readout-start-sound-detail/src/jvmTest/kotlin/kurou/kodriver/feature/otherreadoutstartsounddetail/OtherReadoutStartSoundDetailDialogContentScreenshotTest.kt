@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Test

class OtherReadoutStartSoundDetailDialogContentScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherReadoutStartSoundDetailDialogContent(
                                uiState = OtherReadoutStartSoundDetailUiState(),
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }

    @Test
    fun `保存済みと異なる種別を選択中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherReadoutStartSoundDetailDialogContent(
                                uiState =
                                    OtherReadoutStartSoundDetailUiState(
                                        selectedType = ReadoutStartSoundType.FORMULA_RADIO,
                                        pendingType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                                    ),
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
