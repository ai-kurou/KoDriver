package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

private val sampleRaceFlags =
    LmuWindowsRaceFlagsData(
        gamePhase = SessionPhase.GREEN_FLAG,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
        startLight = 0,
        numRedLights = 0,
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )

private val sampleVirtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = 10)

private fun sampleWheel(surfaceTemperatureCelsius: Double) =
    LmuWindowsTyreWheelData(
        surfaceTemperatureK = surfaceTemperatureCelsius + 273.15,
        carcassTemperatureK = 0.0,
        brakeTemperatureC = 0.0,
        pressureKpa = 0.0,
        wear = 0.0,
    )

private val sampleLmuWindowsTelemetry =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres =
            LmuWindowsTyreData(
                wheels =
                    mapOf(
                        WheelIndex.FRONT_LEFT to sampleWheel(85.0),
                        WheelIndex.FRONT_RIGHT to sampleWheel(86.0),
                        WheelIndex.REAR_LEFT to sampleWheel(87.0),
                        WheelIndex.REAR_RIGHT to sampleWheel(88.0),
                    ),
            ),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 83_456L,
                sector1Ms = 0L,
                sector1And2Ms = 0L,
                currentLap = 3,
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

private val sampleGt7Ps5Telemetry =
    Gt7Ps5TelemetryData(
        lapCount = 3,
        lapsInRace = 0,
        bestLapTimeMs = 90_000,
        gasLevel = 0f,
        gasCapacity = 0f,
    )

private val sampleVehicleApproach =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(4),
        sideBySideRightVehicleIds = setOf(7),
        lateralDistanceLeftMeters = 2.0,
        lateralDistanceRightMeters = 1.5,
    )

class DebugStateDetailPaneScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト データ未取得`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        DebugStateDetailPaneContent(
                            uiState = DebugStateDetailUiState(),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    private val allCardsFilledUiState =
        DebugStateDetailUiState(
            selectedSimulator = Simulator.LmuWindows,
            raceFlags = sampleRaceFlags,
            virtualEnergy = sampleVirtualEnergy,
            lmuWindowsTelemetry = sampleLmuWindowsTelemetry,
            gt7Ps5Telemetry = sampleGt7Ps5Telemetry,
            vehicleApproach = sampleVehicleApproach,
        )

    @Test
    fun `全カードにデータ取得済み`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        DebugStateDetailPaneContent(
                            uiState = allCardsFilledUiState,
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `全カードにデータ取得済み スクロール後は残りのカードが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        DebugStateDetailPaneContent(
                            uiState = allCardsFilledUiState,
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }
            }
        }
        rule
            .onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
            .performScrollToIndex(allCardsFilledUiState.cardOrder.lastIndex)
        rule.onRoot().captureRoboImage()
    }
}
