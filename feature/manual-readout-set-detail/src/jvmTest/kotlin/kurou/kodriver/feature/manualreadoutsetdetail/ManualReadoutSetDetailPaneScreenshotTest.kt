package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class ManualReadoutSetDetailPaneScreenshotTest {
    @Test
    fun `項目あり`() = screenshot(listOf("energy"))

    @Test
    fun `項目なし`() = screenshot(emptyList())

    private fun screenshot(keys: List<String>) =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            ManualReadoutSetDetailPane(
                                uiState = ManualReadoutSetDetailUiState(setNumber = 1, availableItemKeys = keys),
                                onToggleItem = {},
                                onDelete = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `削除ダイアログ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        ManualReadoutSetDeleteConfirmDialog({}, {})
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
