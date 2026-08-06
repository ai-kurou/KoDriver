package kurou.kodriver.feature.acewindowsreadout.flagdetail

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

class AceWindowsReadoutFlagDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
            )
        }

    @Test
    fun `全カード無効`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to false },
            )
        }

    private fun DesktopComposeUiTest.captureAceWindowsReadoutFlagDetailPane(
        enabledStates: Map<ReadoutItemKey, Boolean>,
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 800.dp)) {
                        AceWindowsReadoutFlagDetailPaneContent(
                            uiState = AceWindowsReadoutFlagDetailUiState(enabledStates = enabledStates),
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
