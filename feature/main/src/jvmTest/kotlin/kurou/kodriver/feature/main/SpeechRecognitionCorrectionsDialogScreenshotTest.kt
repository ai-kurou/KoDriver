package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.util.SPEECH_RECOGNITION_CORRECTIONS
import org.junit.Test

class SpeechRecognitionCorrectionsDialogScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            SpeechRecognitionCorrectionsDialog(
                                corrections = SPEECH_RECOGNITION_CORRECTIONS,
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
