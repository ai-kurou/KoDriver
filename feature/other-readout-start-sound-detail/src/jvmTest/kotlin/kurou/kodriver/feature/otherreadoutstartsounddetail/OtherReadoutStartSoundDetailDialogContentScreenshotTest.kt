@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Rule
import org.junit.Test

class OtherReadoutStartSoundDetailDialogContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherReadoutStartSoundDetailDialogContent(
                            uiState = OtherReadoutStartSoundDetailUiState(),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `電子ノイズ選択中`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherReadoutStartSoundDetailDialogContent(
                            uiState = OtherReadoutStartSoundDetailUiState(
                                selectedType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                                pendingType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                            ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `保存済みと異なる種別を選択中`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        OtherReadoutStartSoundDetailDialogContent(
                            uiState = OtherReadoutStartSoundDetailUiState(
                                selectedType = ReadoutStartSoundType.FORMULA_RADIO,
                                pendingType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                            ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }
}
