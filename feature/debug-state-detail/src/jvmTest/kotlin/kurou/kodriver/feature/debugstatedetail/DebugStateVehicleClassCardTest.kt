package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.core.model.Gt7Ps5VehicleClassData
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.core.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleClassCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = null,
                            lmuWindowsVehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar"),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_CLASS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7でクラス名が空文字列の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            gt7Ps5VehicleClass = Gt7Ps5VehicleClassData(name = ""),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_CLASS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            lmuWindowsVehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar"),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_CLASS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `クラス名が空文字列の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsVehicleClass = LmuWindowsVehicleClassData.fromRawValue(""),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_CLASS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はクラス名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsVehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar"),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("Hypercar").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合はクラス名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            gt7Ps5VehicleClass = Gt7Ps5VehicleClassData(name = "Gr.3"),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_CLASS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("Gr.3").assertIsDisplayed()
    }
}
