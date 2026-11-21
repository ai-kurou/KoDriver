package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Test

class TtsUnavailableDialogScreenshotTest {
    @Test
    fun `エンジン不足 ライトテーマ`() =
        composeScreenshotTest {
            capture(TtsUnavailableGuidance.EngineMissing)
        }

    @Test
    fun `日本語データ不足 ライトテーマ`() =
        composeScreenshotTest {
            capture(TtsUnavailableGuidance.LanguageDataMissing)
        }

    @Test
    fun `Windows音声不足 ライトテーマ`() =
        composeScreenshotTest {
            capture(TtsUnavailableGuidance.WindowsSpeechUnavailable)
        }

    private fun DesktopComposeUiTest.capture(reason: TtsUnavailableGuidance) {
        setContent {
            AppTheme(darkTheme = false) {
                Surface {
                    Box(modifier = Modifier.requiredSize(width = 480.dp, height = 320.dp)) {
                        TtsUnavailableDialog(reason, onPrimaryClick = {}, onDismiss = {})
                    }
                }
            }
        }
        onNode(isDialog()).captureRoboImage()
    }
}
