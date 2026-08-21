package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateTyreWearCardTest {
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
                            lmuWindowsTelemetry =
                                sampleLmuWindowsTelemetry(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            lmuWindowsTelemetry =
                                sampleLmuWindowsTelemetry(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
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
                            lmuWindowsTelemetry =
                                sampleLmuWindowsTelemetry(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUでもlmuWindowsTelemetryがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsTelemetry = null,
                            cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsTelemetry =
                                sampleLmuWindowsTelemetry(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
        rule.onNodeWithText("FR -%").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪の摩耗率をパーセントで表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsTelemetry =
                                sampleLmuWindowsTelemetry(
                                    wheels =
                                        mapOf(
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

        rule.onNodeWithText("タイヤ摩耗").assertIsDisplayed()
        rule.onNodeWithText("FL 80.0%").assertIsDisplayed()
        rule.onNodeWithText("FR 75.0%").assertIsDisplayed()
        rule.onNodeWithText("RL 70.0%").assertIsDisplayed()
        rule.onNodeWithText("RR 65.0%").assertIsDisplayed()
    }

    private fun sampleWheel(wear: Double) =
        LmuWindowsTyreWheelData(
            surfaceTemperature = CelsiusReading(0f),
            carcassTemperature = CelsiusReading(0f),
            brakeTemperature = CelsiusReading(0f),
            pressureKpa = PressureKpa(0.0),
            wear = LmuWindowsTyreWearRatio(wear),
        )

    private fun sampleLmuWindowsTelemetry(wheels: Map<WheelIndex, LmuWindowsTyreWheelData>) =
        LmuWindowsTelemetryData(
            timestampMs = 0L,
            engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
            inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
            tyres = LmuWindowsTyreData(wheels = wheels),
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
}
