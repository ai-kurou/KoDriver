package kurou.kodriver.feature.narratoroverlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Test

class NarratorOverlayContentScreenshotTest {
    @Test
    fun `読み上げ内容がない場合`() =
        composeScreenshotTest {
            captureNarratorOverlayContent(uiState = NarratorOverlayUiState(latestTelemetryLog = null))
        }

    @Test
    fun `短い読み上げ内容`() =
        composeScreenshotTest {
            captureNarratorOverlayContent(
                uiState = NarratorOverlayUiState(latestTelemetryLog = telemetryLog(narratedText = "イエローフラッグ")),
            )
        }

    @Test
    fun `長い読み上げ内容は折り返して表示される`() =
        composeScreenshotTest {
            captureNarratorOverlayContent(
                uiState =
                    NarratorOverlayUiState(
                        latestTelemetryLog =
                            telemetryLog(
                                narratedText = "コーナー進入注意。左後方から車両が接近しています。ブレーキングポイントに注意してください。",
                            ),
                    ),
            )
        }

    private fun DesktopComposeUiTest.captureNarratorOverlayContent(uiState: NarratorOverlayUiState) {
        setContent {
            Box(modifier = Modifier.requiredSize(400.dp, 120.dp)) {
                NarratorOverlayContent(uiState = uiState)
            }
        }
        onRoot().captureRoboImage()
    }

    private fun telemetryLog(narratedText: String) =
        TelemetryLog(
            id = 1L,
            createdAt = 1_000L,
            simulator = Simulator.AceWindows,
            readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuel.Root,
            narratedText = narratedText,
            telemetryJson = "{}",
        )
}
