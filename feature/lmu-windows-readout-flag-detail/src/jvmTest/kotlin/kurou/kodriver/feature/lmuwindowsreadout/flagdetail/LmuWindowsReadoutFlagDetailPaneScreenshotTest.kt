package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Test

class LmuWindowsReadoutFlagDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates =
                    mapOf(
                        ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                        ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
                    ),
            )
        }

    private fun DesktopComposeUiTest.captureLmuWindowsReadoutFlagDetailPane(
        enabledStates: Map<ReadoutItemKey, Boolean>,
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        LmuWindowsReadoutFlagDetailPaneContent(
                            uiState = LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates),
                            onFlagEnabledChanged = { _, _ -> },
                            onPreviewClicked = {},
                            onRedFlagEnabledChanged = {},
                            onRedFlagVoiceTypeChanged = {},
                            onRedFlagPreviewClicked = {},
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
