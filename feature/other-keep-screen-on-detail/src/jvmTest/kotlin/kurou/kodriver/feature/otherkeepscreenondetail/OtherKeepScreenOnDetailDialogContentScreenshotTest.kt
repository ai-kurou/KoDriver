@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherkeepscreenondetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class OtherKeepScreenOnDetailDialogContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherKeepScreenOnDetailDialogContent(
                            uiState = OtherKeepScreenOnDetailUiState(),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `OFFを選択中`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherKeepScreenOnDetailDialogContent(
                            uiState = OtherKeepScreenOnDetailUiState(
                                keepScreenOn = true,
                                pendingKeepScreenOn = false,
                            ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }
}
