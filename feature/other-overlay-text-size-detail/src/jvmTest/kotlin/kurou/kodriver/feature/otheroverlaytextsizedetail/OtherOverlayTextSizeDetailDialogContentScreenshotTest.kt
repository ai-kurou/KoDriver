@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.OverlayTextSize
import org.junit.Test

class OtherOverlayTextSizeDetailDialogContentScreenshotTest {
    @Test
    fun `中を選択中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherOverlayTextSizeDetailDialogContent(
                                uiState =
                                    OtherOverlayTextSizeDetailUiState(
                                        pendingOverlayTextSize = OverlayTextSize.MEDIUM,
                                    ),
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
