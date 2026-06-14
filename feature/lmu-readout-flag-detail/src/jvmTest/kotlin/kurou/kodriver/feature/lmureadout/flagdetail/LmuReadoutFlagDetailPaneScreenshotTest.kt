package kurou.kodriver.feature.lmureadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Rule
import org.junit.Test

class LmuReadoutFlagDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        captureLmuReadoutFlagDetailPane(
            enabledStates = mapOf(
                ReadoutItemKey.BLUE_FLAG to true,
                ReadoutItemKey.SECTOR_YELLOW_FLAG to true,
                ReadoutItemKey.FULL_COURSE_YELLOW to true,
                ReadoutItemKey.RED_FLAG to true,
            ),
        )
    }

    @Test
    fun `全カード無効`() {
        captureLmuReadoutFlagDetailPane(
            enabledStates = mapOf(
                ReadoutItemKey.BLUE_FLAG to false,
                ReadoutItemKey.SECTOR_YELLOW_FLAG to false,
                ReadoutItemKey.FULL_COURSE_YELLOW to false,
                ReadoutItemKey.RED_FLAG to false,
            ),
        )
    }

    private fun captureLmuReadoutFlagDetailPane(enabledStates: Map<String, Boolean>) {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        LmuReadoutFlagDetailPaneContent(
                            uiState = LmuReadoutFlagDetailUiState(enabledStates = enabledStates),
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
