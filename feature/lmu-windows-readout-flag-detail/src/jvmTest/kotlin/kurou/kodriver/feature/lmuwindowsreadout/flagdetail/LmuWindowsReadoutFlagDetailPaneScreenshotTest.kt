package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutFlagDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        captureLmuWindowsReadoutFlagDetailPane(
            enabledStates = mapOf(
                ReadoutItemKey.BlueFlag to true,
                ReadoutItemKey.SectorYellowFlag to true,
                ReadoutItemKey.FullCourseYellow to true,
                ReadoutItemKey.RedFlag to true,
            ),
        )
    }

    @Test
    fun `全カード無効`() {
        captureLmuWindowsReadoutFlagDetailPane(
            enabledStates = mapOf(
                ReadoutItemKey.BlueFlag to false,
                ReadoutItemKey.SectorYellowFlag to false,
                ReadoutItemKey.FullCourseYellow to false,
                ReadoutItemKey.RedFlag to false,
            ),
        )
    }

    private fun captureLmuWindowsReadoutFlagDetailPane(enabledStates: Map<ReadoutItemKey, Boolean>) {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        LmuWindowsReadoutFlagDetailPaneContent(
                            uiState = LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates),
                            onFlagEnabledChanged = { _, _ -> },
                            onPreviewClicked = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
