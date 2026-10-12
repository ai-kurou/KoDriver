package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import org.junit.Rule
import org.junit.Test

class DebugStateBestLapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ベストラップが未計測（0以下）の場合は未取得の文言を表示する`() {
        listOf(0L, -1L).forEach { bestLapTimeMs ->
            rule.setContent {
                MaterialTheme {
                    LmuWindowsDebugStateDetailPaneContent(
                        uiState =
                            LmuWindowsDebugStateDetailUiState(
                                cardOrder = listOf(LmuWindowsDebugStateCardKey.BEST_LAP),
                                lmuWindows =
                                    LmuWindowsDebugState(
                                        telemetry = sampleLmuWindowsTelemetry(bestLapTimeMs),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
            rule.onNodeWithText("未取得").assertIsDisplayed()
        }
    }

    @Test
    fun `LMUテレメトリのbestLapTimeMsを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            lmuWindows =
                                LmuWindowsDebugState(
                                    telemetry = sampleLmuWindowsTelemetry(bestLapTimeMs = 83_456L),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        val cardIndex = defaultDebugStateCardOrder.indexOf(LmuWindowsDebugStateCardKey.BEST_LAP)
        rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).performScrollToIndex(cardIndex)
        rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
        rule.onNodeWithText("1:23.456").assertIsDisplayed()
    }

    private fun sampleLmuWindowsTelemetry(bestLapTimeMs: Long) =
        LmuWindowsTelemetryData(
            timestampMs = 0L,
            engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
            inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
            tyres = LmuWindowsTyreData(wheels = emptyMap()),
            fuel =
                LmuWindowsFuelData(
                    currentLiters = LmuWindowsFuelUnit(0.0),
                    capacityLiters = LmuWindowsFuelUnit(0.0),
                ),
            timing =
                LmuWindowsTimingData(
                    currentLapTimeMs = 0L,
                    lastLapTimeMs = 0L,
                    bestLapTimeMs = bestLapTimeMs,
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
