package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DetailPaneCardTextFieldScreenshotTest {
    @Test
    fun `未入力`() = composeScreenshotTest { captureCard(value = "") }

    @Test
    fun `カスタム文言あり`() = composeScreenshotTest { captureCard(value = "イエロー、前方注意") }

    @Test
    fun `TTSを利用できない`() = composeScreenshotTest { captureCard(value = "", enabled = false) }

    @Test
    fun `カスタム文言あり ダークテーマ`() = composeScreenshotTest { captureCard("イエロー、前方注意", darkTheme = true) }

    private fun DesktopComposeUiTest.captureCard(
        value: String,
        enabled: Boolean = true,
        darkTheme: Boolean = false,
    ) {
        setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 260.dp)) {
                        DetailPaneCard(
                            title = "イエローフラッグ",
                            checked = true,
                            onCheckedChange = {},
                            modifier = Modifier.padding(16.dp),
                            bottomContent = {
                                DetailPaneCardChips(
                                    chipLabels = listOf("イエローフラッグ"),
                                    selectedChipLabels = setOf("イエローフラッグ"),
                                    chipEnabled = true,
                                    onChipClick = {},
                                )
                                DetailPaneCardTextField(
                                    value = value,
                                    placeholder = "イエローフラッグ",
                                    maxLength = 30,
                                    onValueChangeFinished = {},
                                    onPreviewClick = {},
                                    enabled = enabled,
                                    supportingText = "空欄のままなら収録音声で読み上げます",
                                )
                            },
                        )
                    }
                }
            }
        }
        onAllNodes(isRoot()).get(0).captureRoboImage()
    }
}
