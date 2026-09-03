package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class ReadoutDetailPaneScreenshotTest {
    @Test
    fun `戻るボタンあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            ReadoutDetailPane(
                                title = "フラッグ",
                                canNavigateBack = true,
                                onBack = {},
                            ) {
                                Text("詳細内容")
                            }
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `戻るボタンなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            ReadoutDetailPane(
                                title = "フラッグ",
                                canNavigateBack = false,
                                onBack = {},
                            ) {
                                Text("詳細内容")
                            }
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }
}
