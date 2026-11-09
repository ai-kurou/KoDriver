package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import org.junit.Test

class ManualReadoutItemPickerSheetBottomScreenshotTest {
    // 全項目だと内容が表示領域を僅かに超えるため、先頭の項目を除いて末尾（下端の余白まで）が収まるようにする。
    // スクロール操作は環境により本文が描画されない場合があるため使わない。
    @Test
    fun `項目追加シート 最下部まで表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            ManualReadoutItemPickerSheetContent(
                                itemKeys = lmuWindowsManualReadoutItemKeys().drop(LEADING_ITEM_COUNT_TO_OMIT),
                                selectedCount = 0,
                                onSelect = {},
                            )
                        }
                    }
                }
            }
            onNodeWithText("クラス内後方車とのギャップ").assertIsDisplayed()
            onRoot().captureRoboImage()
        }
}

private const val LEADING_ITEM_COUNT_TO_OMIT = 4
