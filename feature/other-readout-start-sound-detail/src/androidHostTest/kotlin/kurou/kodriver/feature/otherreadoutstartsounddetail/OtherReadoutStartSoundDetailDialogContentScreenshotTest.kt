@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w480dp-h320dp")
class OtherReadoutStartSoundDetailDialogContentScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
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
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
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
