package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
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
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            ManualReadoutItemPickerSheetContent(
                                itemKeys = lmuWindowsManualReadoutItemKeys(),
                                selectedCount = 0,
                                onSelect = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(hasScrollAction()).onFirst().performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, SCROLL_TO_BOTTOM_PX)
            }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

// 内容は表示領域を十数dp超える程度のため、それを超える量を指定して最下部で打ち切らせ、位置を固定する。
// 大きすぎる値（100_000f 等）では本文が表示領域外へ出てしまい、ヘッダーだけの画像になる。
private const val SCROLL_TO_BOTTOM_PX = 1_000f
