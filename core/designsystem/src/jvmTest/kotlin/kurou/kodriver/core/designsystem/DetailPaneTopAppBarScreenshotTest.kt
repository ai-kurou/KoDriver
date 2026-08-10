package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DetailPaneTopAppBarScreenshotTest {
    @Test
    fun `戻るボタンあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = true,
                                navigateBackContentDescription = "戻る",
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `戻るボタンなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = false,
                                navigateBackContentDescription = "戻る",
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
