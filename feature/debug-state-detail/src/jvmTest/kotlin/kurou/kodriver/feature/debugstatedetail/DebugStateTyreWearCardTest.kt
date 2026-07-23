package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateTyreWearCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイヤ摩耗カードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪の摩耗率をパーセントで表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(
                            wheels = mapOf(
                                WheelIndex.FRONT_LEFT to sampleWheel(0.8),
                                WheelIndex.FRONT_RIGHT to sampleWheel(0.75),
                                WheelIndex.REAR_LEFT to sampleWheel(0.7),
                                WheelIndex.REAR_RIGHT to sampleWheel(0.65),
                            ),
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("FL 80.0%").assertIsDisplayed()
        rule.onNodeWithText("FR 75.0%").assertIsDisplayed()
        rule.onNodeWithText("RL 70.0%").assertIsDisplayed()
        rule.onNodeWithText("RR 65.0%").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(
                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                        ),
                        cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
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
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = null,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(
                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                        ),
                        cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        lmuWindowsTelemetry = sampleLmuWindowsTelemetry(
                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                        ),
                        cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("FR -%").assertIsDisplayed()
    }

    private fun sampleWheel(wear: Double) = LmuWindowsTyreWheelData(
        surfaceTemperatureK = 0.0,
        carcassTemperatureK = 0.0,
        brakeTemperatureC = 0.0,
        pressureKpa = 0.0,
        wear = wear,
    )

    private fun sampleLmuWindowsTelemetry(wheels: Map<WheelIndex, LmuWindowsTyreWheelData>) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = wheels),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing = LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = 0L,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = 0,
            maxLaps = 0,
        ),
        vehicle = LmuWindowsVehicleData(
            localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
            positionX = 0.0, positionY = 0.0, positionZ = 0.0,
        ),
    )
}
