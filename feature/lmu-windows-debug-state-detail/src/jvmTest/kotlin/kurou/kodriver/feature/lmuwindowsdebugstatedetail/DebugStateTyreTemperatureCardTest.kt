package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
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
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateTyreTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `テレメトリが未取得の場合は未取得と表示する`() {
        rule.setContent {
            MaterialTheme {
                TyreTemperatureContent(lmuWindowsTelemetry = null)
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `ホイールが空の場合は全輪の温度をハイフンで表示する`() {
        rule.setContent {
            MaterialTheme {
                TyreTemperatureContent(sampleLmuWindowsTelemetry(wheels = emptyMap()))
            }
        }

        listOf("FL -℃", "FR -℃", "RL -℃", "RR -℃").forEach { text ->
            rule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE),
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
    fun `4輪の表面温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
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

        val cardIndex = defaultDebugStateCardOrder.indexOf(LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE)
        rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).performScrollToIndex(cardIndex)
        rule.onNodeWithText("タイヤ表面温度").assertIsDisplayed()
        rule.onNodeWithText("FL 85.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 86.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 87.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 88.0℃").assertIsDisplayed()
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
