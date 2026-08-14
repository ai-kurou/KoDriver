package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
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
    fun `戻るボタンあり ダークテーマ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = true) {
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

    @Test
    fun `Hazeでぼかす`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            val hazeState = remember { HazeState() }
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .hazeSource(state = hazeState)
                                        .background(Color.Red),
                            )
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = true,
                                navigateBackContentDescription = "戻る",
                                onBack = {},
                                hazeState = hazeState,
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
