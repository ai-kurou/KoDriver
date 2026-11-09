package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import org.junit.Test

class ManualReadoutItemPickerSheetBottomScreenshotTest {
    @Test
    fun `項目追加シート 最下部までスクロール`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, HOST_HEIGHT_DP.dp)) {
                            ManualReadoutItemPickerSheetContent(
                                itemKeys = lmuWindowsManualReadoutItemKeys(),
                                selectedCount = 0,
                                onSelect = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(hasScrollAction()).onFirst().performTouchInput { repeat(SCROLL_TO_BOTTOM_SWIPE_COUNT) { swipeUp() } }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

// 内容より十分低い表示領域にして、スワイプで確実に最下部まで到達させる（ほぼ同じ高さだと最下部の位置が安定しない）。
private const val HOST_HEIGHT_DP = 600
private const val SCROLL_TO_BOTTOM_SWIPE_COUNT = 5
