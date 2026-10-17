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
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.WheelIndex
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import org.junit.Rule
import org.junit.Test

class DebugStateTyreWearCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `lmuWindowsTelemetryがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.TYRE_WEAR),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry = null,
                                ),
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
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.TYRE_WEAR),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
                                        sampleLmuWindowsTelemetry(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to sampleWheel(0.8)),
                                        ),
                                ),
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
    fun `4輪の摩耗率をパーセントで表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.TYRE_WEAR),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry =
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
