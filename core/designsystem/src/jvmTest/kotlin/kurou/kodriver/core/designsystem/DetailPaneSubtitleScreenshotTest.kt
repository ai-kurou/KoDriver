package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DetailPaneSubtitleScreenshotTest {
    @Test
    fun `trailingContentあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 160.dp)) {
                            DetailPaneSubtitle(
                                text = "詳細設定",
                                trailingContent = {
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Filled.Info, contentDescription = null)
                                    }
                                },
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `trailingContentなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 160.dp)) {
                            DetailPaneSubtitle(
                                text = "詳細設定",
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
