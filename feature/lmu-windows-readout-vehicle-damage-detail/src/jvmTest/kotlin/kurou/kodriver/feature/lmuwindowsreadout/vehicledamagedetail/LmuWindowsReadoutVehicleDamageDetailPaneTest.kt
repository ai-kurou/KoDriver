package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.domain.model.OverheatVoiceType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleDamageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `過熱警告の有効状態を表示して操作できる`() {
        var enabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(overheatEnabled = true),
                    onOverheatEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onAllNodesWithText("オーバーヒート")[0].assertIsDisplayed().performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun `GP2チップをタップするとonOverheatVoiceTypeChangedとonPreviewClickedが呼ばれる`() {
        var changedVoiceType: OverheatVoiceType? = null
        var previewedVoiceType: OverheatVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatVoiceType = OverheatVoiceType.STANDARD,
                        ),
                    onOverheatVoiceTypeChanged = { changedVoiceType = it },
                    onPreviewClicked = { previewedVoiceType = it },
                )
            }
        }

        rule.onAllNodesWithText("GP2 GP2… ahhh!!!")[0].assertIsEnabled().performClick()

        assertEquals(OverheatVoiceType.GP2_GP2, changedVoiceType)
        assertEquals(OverheatVoiceType.GP2_GP2, previewedVoiceType)
    }

    @Test
    fun `standardチップをタップするとonOverheatVoiceTypeChangedとonPreviewClickedが呼ばれる`() {
        var changedVoiceType: OverheatVoiceType? = null
        var previewedVoiceType: OverheatVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatVoiceType = OverheatVoiceType.GP2_GP2,
                        ),
                    onOverheatVoiceTypeChanged = { changedVoiceType = it },
                    onPreviewClicked = { previewedVoiceType = it },
                )
            }
        }

        rule.onAllNodesWithText("オーバーヒート")[1].assertIsEnabled().performClick()

        assertEquals(OverheatVoiceType.STANDARD, changedVoiceType)
        assertEquals(OverheatVoiceType.STANDARD, previewedVoiceType)
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

        rule.onAllNodesWithText("GP2 GP2… ahhh!!!")[0].assertIsNotEnabled()
        rule.onAllNodesWithText("オーバーヒート")[1].assertIsNotEnabled()
    }

    @Test
    fun `部品脱落の有効状態を表示して操作できる`() {
        var enabled: Boolean? = null
        var previewCount = 0
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(partDetachedEnabled = true),
                    onPartDetachedEnabledChanged = { enabled = it },
                    onPartDetachedPreviewClicked = { previewCount++ },
                )
            }
        }

        rule.onAllNodesWithText("部品脱落")[0].assertIsDisplayed().performClick()
        assertEquals(false, enabled)

        rule.onAllNodesWithText("部品脱落")[1].assertIsEnabled().performClick()
        assertEquals(1, previewCount)
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
