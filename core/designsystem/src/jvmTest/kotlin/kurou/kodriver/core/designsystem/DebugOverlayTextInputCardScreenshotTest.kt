package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DebugOverlayTextInputCardScreenshotTest {
    @Test
    fun `入力した文字列と読み上げ項目を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(360.dp, 320.dp)) {
                            DebugOverlayTextInputCard(
                                text = "オーバーレイ表示テスト",
                                selectedKey = "gt7_ps5_my_best_lap",
                                keys = listOf("gt7_ps5_my_best_lap"),
                                onTextChange = {},
                                onKeySelect = {},
                                onSend = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
