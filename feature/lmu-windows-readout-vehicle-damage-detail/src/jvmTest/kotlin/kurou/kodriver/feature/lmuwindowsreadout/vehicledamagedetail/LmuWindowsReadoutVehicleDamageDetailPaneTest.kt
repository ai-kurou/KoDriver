package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleDamageDetailPaneTest {
    @Test
    fun `過熱警告の有効状態を表示して操作できる`() =
        composeScreenshotTest {
            var enabled: Boolean? = null
            var previewCount = 0
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutVehicleDamageDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true),
                        onOverheatEnabledChanged = { enabled = it },
                        onPreviewClicked = { previewCount++ },
                    )
                }
            }

            onNodeWithText("オーバーヒート").assertIsDisplayed().performClick()
            assertEquals(false, enabled)

            onNodeWithText("GP2 GP2… ahhh!!!").assertIsEnabled().performClick()
            assertEquals(1, previewCount)
        }

    @Test
    fun `過熱警告が無効ならプレビューチップも無効になる`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutVehicleDamageDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = false),
                    )
                }
            }

            onNodeWithText("GP2 GP2… ahhh!!!").assertIsNotEnabled()
        }
}
