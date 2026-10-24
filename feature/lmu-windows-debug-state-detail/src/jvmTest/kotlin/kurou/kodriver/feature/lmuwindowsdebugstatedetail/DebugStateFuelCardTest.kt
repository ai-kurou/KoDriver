package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import org.junit.Rule
import org.junit.Test

class DebugStateFuelCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `燃料残量と容量割合を表示する`() {
        showFuelCard(telemetry(currentLiters = 42.5, capacityLiters = 100.0))

        rule.onNodeWithText("燃料").assertIsDisplayed()
        rule.onNodeWithText("残量 42.5 L").assertIsDisplayed()
        rule.onNodeWithText("残量 42.5%").assertIsDisplayed()
        rule.onNodeWithText("消費").assertDoesNotExist()
    }

    @Test
    fun `容量が0の場合はダミー値とみなし未取得を表示する`() {
        showFuelCard(telemetry(currentLiters = 0.0, capacityLiters = 0.0))

        rule.onNodeWithText("未取得").assertIsDisplayed()
        rule.onNodeWithText("残量 0.0 L").assertDoesNotExist()
    }

    @Test
    fun `telemetryがnullの場合は未取得を表示する`() {
        showFuelCard(null)

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun showFuelCard(telemetry: LmuWindowsTelemetryData?) {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION),
                            lmuWindows = LmuWindowsDebugState(telemetry = telemetry),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }
    }

    private fun telemetry(
        currentLiters: Double,
        capacityLiters: Double,
    ) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(LmuWindowsFuelUnit(currentLiters), LmuWindowsFuelUnit(capacityLiters)),
        timing = LmuWindowsTimingData(0L, 0L, 0L, 0L, 0L, 1, 0),
        vehicle = LmuWindowsVehicleData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    )
}
