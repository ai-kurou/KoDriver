package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
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
import org.junit.Test

class DebugStateTyreTemperatureCardTest {
    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = null,
                                lmuWindowsTelemetry =
                                    sampleLmuWindowsTelemetry(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤ表面温度").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.Gt7Ps5,
                                lmuWindowsTelemetry =
                                    sampleLmuWindowsTelemetry(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤ表面温度").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがACEの場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.AceWindows,
                                lmuWindowsTelemetry =
                                    sampleLmuWindowsTelemetry(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤ表面温度").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                lmuWindowsTelemetry =
                                    sampleLmuWindowsTelemetry(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤ表面温度").assertIsDisplayed()
            onNodeWithText("FR -℃").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪の表面温度を摂氏で表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                lmuWindowsTelemetry =
                                    sampleLmuWindowsTelemetry(
                                        wheels =
                                            mapOf(
                                                WheelIndex.FRONT_LEFT to sampleWheel(85.0),
                                                WheelIndex.FRONT_RIGHT to sampleWheel(86.0),
                                                WheelIndex.REAR_LEFT to sampleWheel(87.0),
                                                WheelIndex.REAR_RIGHT to sampleWheel(88.0),
                                            ),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤ表面温度").assertIsDisplayed()
            onNodeWithText("FL 85.0℃").assertIsDisplayed()
            onNodeWithText("FR 86.0℃").assertIsDisplayed()
            onNodeWithText("RL 87.0℃").assertIsDisplayed()
            onNodeWithText("RR 88.0℃").assertIsDisplayed()
        }

    private fun sampleWheel(surfaceTemperatureCelsius: Double) =
        LmuWindowsTyreWheelData(
            surfaceTemperatureK = surfaceTemperatureCelsius + 273.15,
            carcassTemperatureK = 0.0,
            brakeTemperatureC = 0.0,
            pressureKpa = 0.0,
            wear = 0.0,
        )

    private fun sampleLmuWindowsTelemetry(wheels: Map<WheelIndex, LmuWindowsTyreWheelData>) =
        LmuWindowsTelemetryData(
            timestampMs = 0L,
            engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
            inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
            tyres = LmuWindowsTyreData(wheels = wheels),
            fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
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
