package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
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

class DebugStateTyreTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7テレメトリがnullの場合はLMUデータがあっても未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
                                        sampleLmuWindowsTelemetry(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
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
                            cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
                                        sampleLmuWindowsTelemetry(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
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
                            cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
                                        sampleLmuWindowsTelemetry(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(85.0)),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
        rule.onNodeWithText("FR -℃").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪の表面温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
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
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
        rule.onNodeWithText("FL 85.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 86.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 87.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 88.0℃").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合は4輪の温度を小数第1位の摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
                                        sampleLmuWindowsTelemetry(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(45.0)),
                                        ),
                                ),
                            gt7Ps5 =
                                Gt7Ps5DebugState(
                                    telemetry =
                                        Gt7Ps5TelemetryData(
                                            lapCount = 3,
                                            lapsInRace = 5,
                                            bestLapTimeMs = 90_000,
                                            gasLevel = Gt7Ps5FuelUnit(20f),
                                            gasCapacity = Gt7Ps5FuelUnit(50f),
                                            tyreTemperature =
                                                Gt7Ps5TyreTemperatureData(
                                                    frontLeftCelsius = CelsiusReading(65.24f),
                                                    frontRightCelsius = CelsiusReading(85.36f),
                                                    rearLeftCelsius = CelsiusReading(105.0f),
                                                    rearRightCelsius = CelsiusReading(115.0f),
                                                ),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
        rule.onNodeWithText("FL 65.2℃").assertIsDisplayed()
        rule.onNodeWithText("FR 85.4℃").assertIsDisplayed()
        rule.onNodeWithText("RL 105.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 115.0℃").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    private fun sampleWheel(surfaceTemperatureCelsius: Double) =
        LmuWindowsTyreWheelData(
            surfaceTemperature = CelsiusReading(surfaceTemperatureCelsius.toFloat()),
            carcassTemperature = CelsiusReading(0f),
            brakeTemperature = CelsiusReading(0f),
            pressureKpa = PressureKpa(0.0),
            wear = LmuWindowsTyreWearRatio(0.0),
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
