package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateFuelConsumptionCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `燃料消費カードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はバーチャルエナジー消費率と残り周数を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
                        lmuWindowsTelemetry = sampleLmuTelemetry(currentLap = 5),
                        cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("消費 10.0%/周").assertIsDisplayed()
        rule.onNodeWithText("残り 5.0周").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合は燃料消費量Lと残り周数を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        gt7Ps5Telemetry = sampleGt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 70f),
                        cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("消費 10.0L/周").assertIsDisplayed()
        rule.onNodeWithText("残り 4.0周").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = null,
                        virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
                        lmuWindowsTelemetry = sampleLmuTelemetry(currentLap = 5),
                        cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun sampleLmuTelemetry(currentLap: Int) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing = LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = 0L,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
        vehicle = LmuWindowsVehicleData(
            localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
            positionX = 0.0, positionY = 0.0, positionZ = 0.0,
        ),
    )

    private fun sampleGt7Telemetry(lapCount: Int, gasLevel: Float, gasCapacity: Float) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = gasLevel,
        gasCapacity = gasCapacity,
    )
}
