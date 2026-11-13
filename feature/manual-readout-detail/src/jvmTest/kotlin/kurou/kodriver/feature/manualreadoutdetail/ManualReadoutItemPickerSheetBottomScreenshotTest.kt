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
    init {
        // 項目定義は登録済みのものだけ表示されるため、単独実行でも本文が出るよう登録する。
        registerAllManualReadoutSimulators()
    }

    // 全項目だと内容が表示領域を僅かに超えるため、先頭の項目を除いて末尾（下端の余白まで）が収まるようにする。
    // スクロール位置に依存しない決定的な撮影にするため、スクロール操作は使わない。
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
            onNodeWithText("クラス内前後車とのギャップ").assertIsDisplayed()
            onRoot().captureRoboImage()
        }
}

private const val LEADING_ITEM_COUNT_TO_OMIT = 4
