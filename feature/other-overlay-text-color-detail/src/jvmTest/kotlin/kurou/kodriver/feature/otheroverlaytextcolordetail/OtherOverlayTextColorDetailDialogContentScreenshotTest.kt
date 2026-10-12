package kurou.kodriver.feature.otheroverlaytextcolordetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import org.junit.Test

class OtherOverlayTextColorDetailDialogContentScreenshotTest {
    @Test
    fun `アンバーを選択中`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherOverlayTextColorDetailDialogContent(
                                uiState =
                                    OtherOverlayTextColorDetailUiState(
                                        pendingOverlayTextColor = OVERLAY_TEXT_COLOR_DEFAULT,
                                    ),
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
