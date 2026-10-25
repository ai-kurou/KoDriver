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
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStatePitTimingRemainingLapsCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `LMUだがバーチャルエネルギーとタイヤ摩耗のデータが取得できない場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエネルギー: 残り -周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り -周").assertIsDisplayed()
        rule.onNodeWithText("燃料: 残り -周").assertIsDisplayed()
    }

    @Test
    fun `バーチャルエネルギーとタイヤ摩耗の予想残り周回数を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    virtualEnergy =
                                        LmuWindowsVirtualEnergyData(
                                            remainingRatio = LmuWindowsVirtualEnergyRatio(0.5),
                                        ),
                                    telemetry =
                                        sampleLmuTelemetry(
                                            currentLap = 5,
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to 0.6),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ピットタイミング予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエネルギー: 残り 5.0周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り 7.5周").assertIsDisplayed()
    }

    @Test
    fun `バーチャルエネルギーだけ未取得でもタイヤ摩耗の残り周回数を表示する`() {
        rule.setContent {
            MaterialTheme {
                PitTimingRemainingLapsContent(
                    virtualEnergy = null,
                    lmuWindowsTelemetry = sampleLmuTelemetry(5, mapOf(WheelIndex.FRONT_LEFT to 0.6)),
                )
            }
        }

        rule.onNodeWithText("バーチャルエネルギー: 残り -周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り 7.5周").assertIsDisplayed()
    }

    @Test
    fun `ホイールが空でもバーチャルエネルギーの残り周回数を表示する`() {
        rule.setContent {
            MaterialTheme {
                PitTimingRemainingLapsContent(
                    virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                    lmuWindowsTelemetry = sampleLmuTelemetry(5, emptyMap()),
                )
            }
        }

        rule.onNodeWithText("バーチャルエネルギー: 残り 5.0周").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗: 残り -周").assertIsDisplayed()
        rule.onNodeWithText("燃料: 残り -周").assertIsDisplayed()
    }

    @Test
    fun `VE未取得でも燃料の予想残り周回数を表示する`() {
        val telemetry =
            sampleLmuTelemetry(5, emptyMap()).copy(
                fuel = LmuWindowsFuelData(LmuWindowsFuelUnit(50.0), LmuWindowsFuelUnit(100.0)),
            )
        rule.setContent {
            MaterialTheme { PitTimingRemainingLapsContent(null, telemetry) }
        }
        rule.onNodeWithText("燃料: 残り 5.0周").assertIsDisplayed()
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
                            surfaceTemperature = CelsiusReading(0f),
                            carcassTemperature = CelsiusReading(0f),
                            brakeTemperature = CelsiusReading(0f),
                            pressureKpa = PressureKpa(0.0),
                            wear = LmuWindowsTyreWearRatio(wear),
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
