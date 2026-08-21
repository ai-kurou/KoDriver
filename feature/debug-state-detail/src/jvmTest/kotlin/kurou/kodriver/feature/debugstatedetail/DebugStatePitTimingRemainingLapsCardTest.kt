package kurou.kodriver.feature.debugstatedetail

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
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStatePitTimingRemainingLapsCardTest {
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
                            cardOrder = listOf(DebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
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
                            cardOrder = listOf(DebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
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
                            cardOrder = listOf(DebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `LMUだがバーチャルエナジーとタイヤ摩耗のデータが取得できない場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            cardOrder = listOf(DebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー: 残り -周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り -周").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はバーチャルエナジーとタイヤ摩耗の予想残り周回数を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
                            lmuWindowsTelemetry =
                                sampleLmuTelemetry(
                                    currentLap = 5,
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to 0.6),
                                ),
                            cardOrder = listOf(DebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー: 残り 5.0周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り 7.5周").assertIsDisplayed()
    }

    private fun sampleLmuTelemetry(
        currentLap: Int,
        wheels: Map<WheelIndex, Double>,
    ) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres =
            LmuWindowsTyreData(
                wheels =
                    wheels.mapValues { (_, wear) ->
                        LmuWindowsTyreWheelData(
                            surfaceTemperatureK = 0.0,
                            carcassTemperatureK = 0.0,
                            brakeTemperatureC = 0.0,
                            pressureKpa = 0.0,
                            wear = wear,
                        )
                    },
            ),
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
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
