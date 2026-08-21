package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateCurrentLapCardTest {
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
                            lmuWindowsTelemetry = sampleLmuWindowsTelemetry(currentLap = 3),
                            gt7Ps5Telemetry = sampleGt7Ps5Telemetry(lapCount = 9),
                            cardOrder = listOf(DebugStateCardKey.CURRENT_LAP),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("現在のラップ").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがAceWindowsの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            lmuWindowsTelemetry = sampleLmuWindowsTelemetry(currentLap = 3),
                            gt7Ps5Telemetry = sampleGt7Ps5Telemetry(lapCount = 9),
                            cardOrder = listOf(DebugStateCardKey.CURRENT_LAP),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("現在のラップ").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はLMUテレメトリのcurrentLapを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsTelemetry = sampleLmuWindowsTelemetry(currentLap = 3),
                            gt7Ps5Telemetry = sampleGt7Ps5Telemetry(lapCount = 9),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("現在のラップ").assertIsDisplayed()
        rule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合はGT7テレメトリのlapCountを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            lmuWindowsTelemetry = sampleLmuWindowsTelemetry(currentLap = 3),
                            gt7Ps5Telemetry = sampleGt7Ps5Telemetry(lapCount = 9),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("現在のラップ").assertIsDisplayed()
        rule.onNodeWithText("9").assertIsDisplayed()
    }

    private fun sampleLmuWindowsTelemetry(currentLap: Int) =
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

    private fun sampleGt7Ps5Telemetry(lapCount: Int) =
        Gt7Ps5TelemetryData(
            lapCount = lapCount,
            lapsInRace = 0,
            bestLapTimeMs = 0,
            gasLevel = Gt7Ps5FuelUnit(0f),
            gasCapacity = Gt7Ps5FuelUnit(0f),
        )
}
