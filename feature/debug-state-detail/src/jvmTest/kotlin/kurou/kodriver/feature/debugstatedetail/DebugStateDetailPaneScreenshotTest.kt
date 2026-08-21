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
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
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
        surfaceTemperature = CelsiusReading(surfaceTemperatureCelsius.toFloat()),
        carcassTemperature = CelsiusReading(0f),
        brakeTemperatureC = 0.0,
        pressureKpa = 0.0,
        wear = LmuWindowsTyreWearRatio(0.0),
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
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
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
        gasLevel = Gt7Ps5FuelUnit(0f),
        gasCapacity = Gt7Ps5FuelUnit(0f),
    )

private val sampleTyreCarcassTemperature =
    LmuWindowsTyreCarcassTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to CelsiusReading(95.0f),
                WheelIndex.FRONT_RIGHT to CelsiusReading(96.0f),
                WheelIndex.REAR_LEFT to CelsiusReading(97.0f),
                WheelIndex.REAR_RIGHT to CelsiusReading(98.0f),
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
