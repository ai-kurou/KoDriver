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
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
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
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.PressureKpa
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
        playerFlag = PrimaryFlag.GREEN,
        playerUnderYellow = false,
    )

private val sampleVirtualEnergy =
    LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5), session = 10)

private fun sampleWheel(surfaceTemperatureCelsius: Double) =
    LmuWindowsTyreWheelData(
        surfaceTemperature = CelsiusReading(surfaceTemperatureCelsius.toFloat()),
        carcassTemperature = CelsiusReading(0f),
        brakeTemperature = CelsiusReading(0f),
        pressureKpa = PressureKpa(0.0),
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

private val sampleBrakeTemperature =
    LmuWindowsBrakeTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to CelsiusReading(320.0f),
                WheelIndex.FRONT_RIGHT to CelsiusReading(325.0f),
                WheelIndex.REAR_LEFT to CelsiusReading(280.0f),
                WheelIndex.REAR_RIGHT to CelsiusReading(285.0f),
            ),
    )

private val sampleBrakeWear =
    LmuWindowsBrakeWearRemainingData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.036f), 87f),
                WheelIndex.FRONT_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.035f), 82.5f),
                WheelIndex.REAR_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), 60f),
                WheelIndex.REAR_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.031f), 65f),
            ),
    )

private val sampleVehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar")

private val sampleAceWindowsStatus =
    AceWindowsStatusData(status = AceWindowsStatusType.LIVE, carLocation = AceWindowsCarLocation.TRACK)

private val sampleLmuWindowsPitStatus =
    LmuWindowsPitStatusData(inPits = true, pitState = LmuWindowsPitState.ENTERING, inGarageStall = false)

private val sampleVehicleDamage =
    LmuWindowsVehicleDamageData(overheating = true, partDetached = false, lastImpactMagnitude = 0.0)

private val sampleTyreDetached =
    LmuWindowsTyreDetachedData(wheels = WheelIndex.entries.associateWith { it == WheelIndex.REAR_LEFT })

private val sampleVehicleApproach =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(4),
        sideBySideRightVehicleIds = setOf(7),
        lateralDistanceLeftMeters = LateralDistanceMeters(2.0),
        lateralDistanceRightMeters = LateralDistanceMeters(1.5),
    )

private val sampleSideBySideDurations = LmuWindowsSideBySideDurations(leftMillis = 3_400, rightMillis = 12_800)

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

    @Test
    fun `GT7のタイヤ表面温度を4段階の色で表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            DebugStateDetailPaneContent(
                                uiState =
                                    DebugStateDetailUiState(
                                        selectedSimulator = Simulator.Gt7Ps5,
                                        gt7Ps5Telemetry =
                                            sampleGt7Ps5Telemetry.copy(
                                                tyreTemperature =
                                                    Gt7Ps5TyreTemperatureData(
                                                        frontLeftCelsius = CelsiusReading(65.2f),
                                                        frontRightCelsius = CelsiusReading(85.4f),
                                                        rearLeftCelsius = CelsiusReading(105f),
                                                        rearRightCelsius = CelsiusReading(115f),
                                                    ),
                                            ),
                                        cardOrder = listOf(DebugStateCardKey.TYRE_TEMPERATURE),
                                        enabledCardKeys = setOf(DebugStateCardKey.TYRE_TEMPERATURE),
                                    ),
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `LMUの並走車両カードに左右の並走継続秒数を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            DebugStateDetailPaneContent(
                                uiState =
                                    DebugStateDetailUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        vehicleApproach = sampleVehicleApproach,
                                        lmuWindowsSideBySideDurations = sampleSideBySideDurations,
                                        cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                                        enabledCardKeys = setOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                                    ),
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
            lmuWindowsSideBySideDurations = sampleSideBySideDurations,
            tyreCarcassTemperature = sampleTyreCarcassTemperature,
            brakeTemperature = sampleBrakeTemperature,
            brakeWear = sampleBrakeWear,
            lmuWindowsVehicleClass = sampleVehicleClass,
            aceWindowsStatus = sampleAceWindowsStatus,
            lmuWindowsPitStatus = sampleLmuWindowsPitStatus,
            vehicleDamage = sampleVehicleDamage,
            tyreDetached = sampleTyreDetached,
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
