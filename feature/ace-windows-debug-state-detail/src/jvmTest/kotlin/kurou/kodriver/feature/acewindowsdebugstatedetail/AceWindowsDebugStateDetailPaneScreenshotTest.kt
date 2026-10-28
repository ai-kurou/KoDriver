package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsCarLocation
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusType
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Test

class AceWindowsDebugStateDetailPaneScreenshotTest {
    @Test
    fun `デフォルト データ未取得`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsDebugStateDetailPaneContent(
                                uiState = AceWindowsDebugStateDetailUiState(),
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
        AceWindowsDebugStateDetailUiState(
            enabledCardKeys = defaultDebugStateCardOrder.toSet(),
            aceWindows =
                AceWindowsDebugState(
                    gap = AceWindowsGapData(1.26f, -2.34f),
                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(42.0)),
                    flag = AceWindowsFlagData(flag = AceWindowsFlagType.YELLOW_FLAG),
                    status = AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.TRACK),
                    bestLapTime = AceWindowsBestLapTimeData(bestLapTimeMs = 83_456),
                    remainingFuelLaps = AceWindowsRemainingFuelLapsData(remainingLaps = 3.46f),
                    vehicleApproach =
                        AceWindowsVehicleApproachData(
                            nearbyVehicles =
                                listOf(
                                    AceWindowsNearbyVehicleData(distanceMeters = 12.34),
                                    AceWindowsNearbyVehicleData(distanceMeters = 3.06),
                                ),
                        ),
                    tyreCarcassTemperature =
                        AceWindowsTyreCarcassTemperatureData(
                            wheels =
                                mapOf(
                                    WheelIndex.FRONT_LEFT to CelsiusReading(69.0f),
                                    WheelIndex.FRONT_RIGHT to CelsiusReading(85.0f),
                                    WheelIndex.REAR_LEFT to CelsiusReading(95.0f),
                                    WheelIndex.REAR_RIGHT to CelsiusReading(110.0f),
                                ),
                        ),
                ),
        )

    @Test
    fun `全カードにデータ取得済み`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsDebugStateDetailPaneContent(
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
