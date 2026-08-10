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

class DetailPaneCardScreenshotTest {
    @Test
    fun `スイッチ付きでON`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 160.dp)) {
                            DetailPaneCard(
                                title = "車両接近",
                                checked = true,
                                onCheckedChange = {},
                                modifier = Modifier.padding(16.dp),
                                bottomContent = {
                                    DetailPaneCardChips(
                                        chipLabels = listOf("カーレフト", "カーライト"),
                                        selectedChipLabels = setOf("カーレフト"),
                                        chipEnabled = true,
                                        onChipClick = {},
                                    )
                                },
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `スイッチ付きでOFF`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 160.dp)) {
                            DetailPaneCard(
                                title = "車両接近",
                                checked = false,
                                onCheckedChange = {},
                                modifier = Modifier.padding(16.dp),
                                bottomContent = {
                                    DetailPaneCardChips(
                                        chipLabels = listOf("カーレフト", "カーライト"),
                                        selectedChipLabels = setOf("カーレフト"),
                                        chipEnabled = false,
                                        onChipClick = {},
                                    )
                                },
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `スイッチなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 160.dp)) {
                            DetailPaneCard(
                                title = "自己ベストラップ更新",
                                modifier = Modifier.padding(16.dp),
                                bottomContent = {
                                    DetailPaneCardChips(
                                        chipLabels = listOf("自己ベストラップ更新"),
                                        selectedChipLabels = setOf("自己ベストラップ更新"),
                                        chipEnabled = true,
                                        onChipClick = {},
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
