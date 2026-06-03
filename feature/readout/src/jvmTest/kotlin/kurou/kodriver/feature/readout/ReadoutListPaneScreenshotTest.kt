package kurou.kodriver.feature.readout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ReadoutListPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        ReadoutListPane(
                            uiState = ReadoutListUiState(
                                simulators = listOf("Le Mans Ultimate"),
                            ),
                            onSimulatorSelected = {},
                            onMoveUp = {},
                            onMoveDown = {},
                            onReadoutEnabledChanged = { _, _ -> },
                            onItemClick = {},
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
