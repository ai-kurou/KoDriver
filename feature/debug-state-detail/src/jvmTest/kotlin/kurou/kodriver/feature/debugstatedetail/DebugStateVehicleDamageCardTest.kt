package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleDamageCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `vehicleDamageがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleDamage = null,
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_DAMAGE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両故障 (LMUのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `オーバーヒートと部品脱落がともにtrueのとき はいを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleDamage =
                                LmuWindowsVehicleDamageData(
                                    overheating = true,
                                    partDetached = true,
                                    lastImpactMagnitude = 0.0,
                                ),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_DAMAGE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("オーバーヒート: はい").assertIsDisplayed()
        rule.onNodeWithText("部品脱落: はい").assertIsDisplayed()
    }

    @Test
    fun `オーバーヒートと部品脱落がともにfalseのとき いいえを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleDamage =
                                LmuWindowsVehicleDamageData(
                                    overheating = false,
                                    partDetached = false,
                                    lastImpactMagnitude = 0.0,
                                ),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_DAMAGE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("オーバーヒート: いいえ").assertIsDisplayed()
        rule.onNodeWithText("部品脱落: いいえ").assertIsDisplayed()
    }
}
