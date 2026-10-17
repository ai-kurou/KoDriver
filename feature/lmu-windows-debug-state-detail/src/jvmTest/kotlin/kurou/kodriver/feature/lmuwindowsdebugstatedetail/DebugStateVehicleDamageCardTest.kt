package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleDamageCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイヤ脱落データに欠損ホイールがある場合はいいえを表示する`() {
        rule.setContent {
            MaterialTheme {
                VehicleDamageContent(
                    vehicleDamage =
                        LmuWindowsVehicleDamageData(
                            overheating = false,
                            partDetached = false,
                            lastImpactMagnitude = 0.0,
                        ),
                    tyreDetached = LmuWindowsTyreDetachedData(wheels = mapOf(WheelIndex.FRONT_LEFT to true)),
                )
            }
        }

        rule.onNodeWithText("タイヤ脱落 FL: はい").assertIsDisplayed()
        listOf("FR", "RL", "RR").forEach { wheel ->
            rule.onNodeWithText("タイヤ脱落 $wheel: いいえ").assertIsDisplayed()
        }
    }

    @Test
    fun `vehicleDamageがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleDamage = null,
                                ),
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
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleDamage =
                                        LmuWindowsVehicleDamageData(
                                            overheating = true,
                                            partDetached = true,
                                            lastImpactMagnitude = 0.0,
                                        ),
                                ),
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
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleDamage =
                                        LmuWindowsVehicleDamageData(
                                            overheating = false,
                                            partDetached = false,
                                            lastImpactMagnitude = 0.0,
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("オーバーヒート: いいえ").assertIsDisplayed()
        rule.onNodeWithText("部品脱落: いいえ").assertIsDisplayed()
    }

    @Test
    fun `タイヤ脱落がnullの場合は全輪いいえを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleDamage =
                                        LmuWindowsVehicleDamageData(
                                            overheating = false,
                                            partDetached = false,
                                            lastImpactMagnitude = 0.0,
                                        ),
                                    tyreDetached = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ脱落 FL: いいえ").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 FR: いいえ").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 RL: いいえ").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 RR: いいえ").assertIsDisplayed()
    }

    @Test
    fun `タイヤ脱落した車輪のみはいを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleDamage =
                                        LmuWindowsVehicleDamageData(
                                            overheating = false,
                                            partDetached = false,
                                            lastImpactMagnitude = 0.0,
                                        ),
                                    tyreDetached =
                                        LmuWindowsTyreDetachedData(
                                            wheels =
                                                WheelIndex.entries.associateWith {
                                                    it == WheelIndex.FRONT_RIGHT
                                                },
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ脱落 FL: いいえ").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 FR: はい").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 RL: いいえ").assertIsDisplayed()
        rule.onNodeWithText("タイヤ脱落 RR: いいえ").assertIsDisplayed()
    }
}
