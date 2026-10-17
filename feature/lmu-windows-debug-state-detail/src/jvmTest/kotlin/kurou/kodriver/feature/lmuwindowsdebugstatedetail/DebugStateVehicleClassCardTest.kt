package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleClassCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `クラス名が空文字列の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_CLASS),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleClass = LmuWindowsVehicleClassData.fromRawValue(""),
                                ),
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
    fun `クラス名を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar"),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("Hypercar").assertIsDisplayed()
    }
}
