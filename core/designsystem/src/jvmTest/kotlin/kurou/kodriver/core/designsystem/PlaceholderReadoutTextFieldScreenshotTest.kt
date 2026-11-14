package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class PlaceholderReadoutTextFieldScreenshotTest {
    @Test
    fun `長いチップ文言とヒントは縦に並ぶ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 260.dp)) {
                            PlaceholderReadoutTextField(
                                label = "読み上げ文言",
                                savedText = "前方{ahead}秒、後方{behind}秒",
                                defaultText = "前方{ahead}秒、後方{behind}秒",
                                maxLength = 40,
                                isTextToSpeechAvailable = true,
                                onTextChanged = {},
                                onPreviewClick = {},
                                placeholder = "{ahead}",
                                insertLabel = "前方{ahead}秒、後方{behind}秒を挿入",
                                placeholderHint = "{ahead}は前の車との差の秒数、{behind}は後ろの車との差の秒数に置き換わります",
                                resetContentDescription = "デフォルトに戻す",
                                previewContentDescription = "試聴",
                                selectedContentDescription = "選択中",
                                unavailableText = "読み上げを利用できません",
                                supportingText = "空欄のままなら読み上げません",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
