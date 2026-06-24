@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherReadoutStartSoundDetailDialogContentScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
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
    }

    @Test
    fun `保存済みと異なる種別を選択中`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
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
    }
}
