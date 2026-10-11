package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import org.junit.Rule
import org.junit.Test

class DebugStateFuelConsumptionCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `バーチャルエナジー残量と消費率と残り周数を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    virtualEnergy =
                                        LmuWindowsVirtualEnergyData(
                                            remainingRatio = LmuWindowsVirtualEnergyRatio(0.5),
                                        ),
                                    telemetry = sampleLmuTelemetry(currentLap = 5),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
        rule.onNodeWithText("残量 50.0%").assertIsDisplayed()
        rule.onNodeWithText("消費 10.0%/周").assertIsDisplayed()
        rule.onNodeWithText("残り 5.0周").assertIsDisplayed()
    }

    @Test
    fun `消費率を計算できなくてもバーチャルエナジー残量を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    virtualEnergy =
                                        LmuWindowsVirtualEnergyData(
                                            remainingRatio = LmuWindowsVirtualEnergyRatio(0.5),
                                        ),
                                    telemetry = sampleLmuTelemetry(currentLap = 0),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
        rule.onNodeWithText("残量 50.0%").assertIsDisplayed()
    }

    @Test
    fun `virtualEnergyがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    virtualEnergy = null,
                                    telemetry = sampleLmuTelemetry(currentLap = 5),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun sampleLmuTelemetry(currentLap: Int) =
        LmuWindowsTelemetryData(
            timestampMs = 0L,
            engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
            inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
            tyres = LmuWindowsTyreData(wheels = emptyMap()),
            fuel =
                LmuWindowsFuelData(
                    currentLiters = LmuWindowsFuelUnit(0.0),
                    capacityLiters = LmuWindowsFuelUnit(0.0),
                ),
            timing =
                LmuWindowsTimingData(
                    currentLapTimeMs = 0L,
                    lastLapTimeMs = 0L,
                    bestLapTimeMs = 0L,
                    sector1Ms = 0L,
                    sector1And2Ms = 0L,
                    currentLap = currentLap,
                    maxLaps = 0,
                ),
            vehicle =
                LmuWindowsVehicleData(
                    localVelocityX = 0.0,
                    localVelocityY = 0.0,
                    localVelocityZ = 0.0,
                    positionX = 0.0,
                    positionY = 0.0,
                    positionZ = 0.0,
                ),
        )
}
