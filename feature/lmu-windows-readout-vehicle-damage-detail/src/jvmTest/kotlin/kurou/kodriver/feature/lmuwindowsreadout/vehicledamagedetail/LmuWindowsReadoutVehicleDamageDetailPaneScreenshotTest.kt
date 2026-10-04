package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class LmuWindowsReadoutVehicleDamageDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() = capture(LmuWindowsReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true))

    @Test
    fun `空白文言`() =
        capture(
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatReadoutText = "",
                partDetachedReadoutText = "",
                tyreDetachedReadoutText = "",
                isTextToSpeechAvailable = true,
            ),
        )

    @Test
    fun `カスタム文言とスイッチOFF`() =
        capture(
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = false,
                partDetachedEnabled = false,
                tyreDetachedEnabled = false,
                overheatReadoutText = "エンジンが過熱しています",
                partDetachedReadoutText = "パーツが外れました",
                tyreDetachedReadoutText = "ホイールが外れました",
                isTextToSpeechAvailable = true,
            ),
        )

    @Test
    fun `TTS利用不可`() = capture(LmuWindowsReadoutVehicleDamageDetailUiState())

    private fun capture(state: LmuWindowsReadoutVehicleDamageDetailUiState) =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutVehicleDamageDetailPaneContent(uiState = state)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
