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
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateBestLapCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ベストラップカードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ベストラップ").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はLMUテレメトリのbestLapTimeMsを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(bestLapTimeMs = 83_456L),
                        gt7Ps5Telemetry = sampleGt7Ps5Telemetry(bestLapTimeMs = 90_000),
                    ),
                        canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("1:23.456").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合はGT7テレメトリのbestLapTimeMsを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(bestLapTimeMs = 83_456L),
                        gt7Ps5Telemetry = sampleGt7Ps5Telemetry(bestLapTimeMs = 90_000),
                    ),
                        canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("1:30.000").assertIsDisplayed()
    }

    @Test
    fun `ベストラップが未計測（0以下）の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        gt7Ps5Telemetry = sampleGt7Ps5Telemetry(bestLapTimeMs = -1),
                        cardOrder = listOf(DebugStateCardKey.BEST_LAP),
                    ),
                        canNavigateBack = true,
                    onBack = {},
                )
            }
        }

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
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(bestLapTimeMs = 83_456L),
                        gt7Ps5Telemetry = sampleGt7Ps5Telemetry(bestLapTimeMs = 90_000),
                        cardOrder = listOf(DebugStateCardKey.BEST_LAP),
                    ),
                        canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                        selectedSimulator = null,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(bestLapTimeMs = 83_456L),
                        gt7Ps5Telemetry = sampleGt7Ps5Telemetry(bestLapTimeMs = 90_000),
                        cardOrder = listOf(DebugStateCardKey.BEST_LAP),
                    ),
                        canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun sampleLmuWindowsTelemetry(bestLapTimeMs: Long) =
        LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = bestLapTimeMs,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = 0,
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

    private fun sampleGt7Ps5Telemetry(bestLapTimeMs: Int) =
        Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 0,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = 0f,
        gasCapacity = 0f,
    )
}
