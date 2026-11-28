package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.Simulator
import org.junit.Test

class OtherWakeWordHotkeyDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() = capture(OtherWakeWordHotkeyDetailUiState(), showHotkey = false)

    @Test
    fun `ホットキー未割り当て`() = capture(OtherWakeWordHotkeyDetailUiState())

    @Test
    fun `ホットキー登録済み`() =
        capture(
            OtherWakeWordHotkeyDetailUiState(
                hotkeyVirtualKeyCode = 0x41,
                hotkeyRegistrationState = HotkeyRegistrationState.Registered,
                speechRecognitionEnabled = true,
            ),
        )

    @Test
    fun `ホットキー登録済みで音声認識がOFF`() =
        capture(
            OtherWakeWordHotkeyDetailUiState(
                hotkeyVirtualKeyCode = 0x41,
                hotkeyRegistrationState = HotkeyRegistrationState.Registered,
            ),
        )

    @Test
    fun `手動読み上げセットと重複`() =
        capture(
            OtherWakeWordHotkeyDetailUiState(
                hotkeyConflict = OtherWakeWordHotkeyConflict(Simulator.LmuWindows.id, 1),
            ),
        )

    private fun capture(
        uiState: OtherWakeWordHotkeyDetailUiState,
        showHotkey: Boolean = true,
    ) = composeScreenshotTest {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        OtherWakeWordHotkeyDetailContent(
                            uiState = uiState,
                            canNavigateBack = true,
                            onBack = {},
                            showHotkey = showHotkey,
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
