package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.model.AceWindowsCarLocation
import kurou.kodriver.core.model.AceWindowsStatusData
import kurou.kodriver.core.model.AceWindowsStatusType
import kurou.kodriver.core.model.CountLapFlag
import kurou.kodriver.core.model.Gt7Ps5TelemetryData
import kurou.kodriver.core.model.LmuWindowsEngineData
import kurou.kodriver.core.model.LmuWindowsFuelData
import kurou.kodriver.core.model.LmuWindowsInputsData
import kurou.kodriver.core.model.LmuWindowsPitState
import kurou.kodriver.core.model.LmuWindowsPitStatusData
import kurou.kodriver.core.model.LmuWindowsRaceFlagsData
import kurou.kodriver.core.model.LmuWindowsTelemetryData
import kurou.kodriver.core.model.LmuWindowsTimingData
import kurou.kodriver.core.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.core.model.LmuWindowsTyreData
import kurou.kodriver.core.model.LmuWindowsTyreWheelData
import kurou.kodriver.core.model.LmuWindowsVehicleApproachData
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.core.model.LmuWindowsVehicleData
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.core.model.PrimaryFlag
import kurou.kodriver.core.model.SectorFlagState
import kurou.kodriver.core.model.SessionPhase
import kurou.kodriver.core.model.SessionYellowFlagState
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.WheelIndex
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

private val sampleTyreCarcassTemperature =
    LmuWindowsTyreCarcassTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to 95.0,
                WheelIndex.FRONT_RIGHT to 96.0,
                WheelIndex.REAR_LEFT to 97.0,
                WheelIndex.REAR_RIGHT to 98.0,
            ),
    )

private val sampleVehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar")

private val sampleAceWindowsStatus =
    AceWindowsStatusData(status = AceWindowsStatusType.LIVE, carLocation = AceWindowsCarLocation.TRACK)

private val sampleLmuWindowsPitStatus =
    LmuWindowsPitStatusData(inPits = true, pitState = LmuWindowsPitState.ENTERING, inGarageStall = false)

private val sampleVehicleApproach =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(4),
        sideBySideRightVehicleIds = setOf(7),
        lateralDistanceLeftMeters = 2.0,
        lateralDistanceRightMeters = 1.5,
    )

class DebugStateDetailPaneScreenshotTest {
    @Test
    fun `デフォルト データ未取得`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            DebugStateDetailPaneContent(
                                uiState = DebugStateDetailUiState(),
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    private val allCardsFilledUiState =
        DebugStateDetailUiState(
            selectedSimulator = Simulator.LmuWindows,
            raceFlags = sampleRaceFlags,
            virtualEnergy = sampleVirtualEnergy,
            lmuWindowsTelemetry = sampleLmuWindowsTelemetry,
            gt7Ps5Telemetry = sampleGt7Ps5Telemetry,
            vehicleApproach = sampleVehicleApproach,
            tyreCarcassTemperature = sampleTyreCarcassTemperature,
            lmuWindowsVehicleClass = sampleVehicleClass,
            aceWindowsStatus = sampleAceWindowsStatus,
            lmuWindowsPitStatus = sampleLmuWindowsPitStatus,
            enabledCardKeys = defaultDebugStateCardOrder.toSet(),
        )

    @Test
    fun `全カードにデータ取得済み`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            DebugStateDetailPaneContent(
                                uiState = allCardsFilledUiState,
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
