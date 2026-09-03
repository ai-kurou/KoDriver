package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleDamageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `過熱警告の有効状態を表示して操作できる`() {
        var enabled: Boolean? = null
        var previewCount = 0
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true),
                    onOverheatEnabledChanged = { enabled = it },
                    onPreviewClicked = { previewCount++ },
                )
            }
        }

        rule.onNodeWithText("オーバーヒート").assertIsDisplayed().performClick()
        assertEquals(false, enabled)

        rule.onNodeWithText("GP2 GP2… ahhh!!!").assertIsEnabled().performClick()
        assertEquals(1, previewCount)
    }

    @Test
    fun `過熱警告が無効ならプレビューチップも無効になる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = false),
                )
            }
        }

        rule.onNodeWithText("GP2 GP2… ahhh!!!").assertIsNotEnabled()
    }

    @Test
    fun `部品脱落の有効状態を表示して操作できる`() {
        var enabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(partDetachedEnabled = true),
                    onPartDetachedEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onAllNodesWithText("部品脱落")[0].assertIsDisplayed().performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun `部品脱落が無効ならプレビューチップも無効になる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(partDetachedEnabled = false),
                )
            }
        }

        rule.onAllNodesWithText("部品脱落")[1].assertIsNotEnabled()
    }
}
