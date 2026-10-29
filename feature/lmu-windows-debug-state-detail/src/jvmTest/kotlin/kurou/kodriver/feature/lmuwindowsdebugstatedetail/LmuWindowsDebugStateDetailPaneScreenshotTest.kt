package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitState
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.lmuwindows.model.PrimaryFlag
import kurou.kodriver.domain.lmuwindows.model.SectorFlagState
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.SessionYellowFlagState
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.PressureKpa
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
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(42.5), capacityLiters = LmuWindowsFuelUnit(100.0)),
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

class LmuWindowsDebugStateDetailPaneScreenshotTest {
    @Test
    fun `現在値読み上げホットキー固定カードを表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        LmuWindowsDebugStateDetailPaneContent(
                            uiState =
                                LmuWindowsDebugStateDetailUiState(
                                    readoutHotkey =
                                        ReadoutHotkeyUiState(registrationState = HotkeyRegistrationState.Registered),
                                ),
                            canNavigateBack = true,
                            onBack = {},
                            modifier = Modifier.requiredSize(1560.dp, 1080.dp),
                        )
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `デフォルト データ未取得`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsDebugStateDetailPaneContent(
                                uiState = LmuWindowsDebugStateDetailUiState(),
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
                            LmuWindowsDebugStateDetailPaneContent(
                                uiState =
                                    LmuWindowsDebugStateDetailUiState(
                                        cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                                        enabledCardKeys = setOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                                        lmuWindows =
                                            LmuWindowsDebugState(
                                                vehicleApproach = sampleVehicleApproach,
                                                sideBySideDurations = sampleSideBySideDurations,
                                            ),
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
    fun `前後車両との時間差カードに符号とゼロを保持して表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsDebugStateDetailPaneContent(
                                uiState =
                                    LmuWindowsDebugStateDetailUiState(
                                        cardOrder = listOf(LmuWindowsDebugStateCardKey.CAR_GAP),
                                        enabledCardKeys = setOf(LmuWindowsDebugStateCardKey.CAR_GAP),
                                        lmuWindows =
                                            LmuWindowsDebugState(
                                                carGap = LmuWindowsCarGapData(1.0, -2.3, 0.0, 4.5, 1.2, null),
                                            ),
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
    fun `同クラス内順位と前後車両との時間差カードに片側の欠損を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsDebugStateDetailPaneContent(
                                uiState =
                                    LmuWindowsDebugStateDetailUiState(
                                        cardOrder = listOf(LmuWindowsDebugStateCardKey.CLASS_CAR_GAP),
                                        enabledCardKeys = setOf(LmuWindowsDebugStateCardKey.CLASS_CAR_GAP),
                                        lmuWindows =
                                            LmuWindowsDebugState(
                                                classCarGap =
                                                    LmuWindowsClassCarGapData(
                                                        1.0,
                                                        null,
                                                        classPosition = 3,
                                                        classVehicleCount = 20,
                                                    ),
                                            ),
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
        LmuWindowsDebugStateDetailUiState(
            enabledCardKeys = defaultDebugStateCardOrder.toSet(),
            lmuWindows =
                LmuWindowsDebugState(
                    carGap = LmuWindowsCarGapData(1.0, -2.3, 4.5, 0.0),
                    classCarGap = LmuWindowsClassCarGapData(1.0, -2.3),
                    raceFlags = sampleRaceFlags,
                    virtualEnergy = sampleVirtualEnergy,
                    virtualEnergyBaseline = LmuWindowsVirtualEnergyBaseline(1.0, 0),
                    tyreWearBaseline = LmuWindowsTyreWearBaseline(1.0, 0),
                    telemetry = sampleLmuWindowsTelemetry,
                    vehicleApproach = sampleVehicleApproach,
                    sideBySideDurations = sampleSideBySideDurations,
                    tyreCarcassTemperature = sampleTyreCarcassTemperature,
                    brakeTemperature = sampleBrakeTemperature,
                    brakeWear = sampleBrakeWear,
                    vehicleClass = sampleVehicleClass,
                    pitStatus = sampleLmuWindowsPitStatus,
                    vehicleDamage = sampleVehicleDamage,
                    tyreDetached = sampleTyreDetached,
                ),
        )

    @Test
    fun `全カードにデータ取得済み`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsDebugStateDetailPaneContent(
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
