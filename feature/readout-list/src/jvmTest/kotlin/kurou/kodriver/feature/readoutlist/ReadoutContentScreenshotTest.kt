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
import kurou.kodriver.buildlogic.screenshottest.singlePaneDirective
import kurou.kodriver.buildlogic.screenshottest.twoPaneDirective
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import org.junit.Test

class ReadoutContentScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            ReadoutContent(
                                uiState =
                                    ReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = ReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        selectedItem = ReadoutListItemType.LmuWindows.Flag,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { item -> Text("Detail: $item") },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `一覧のみ表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            ReadoutContent(
                                uiState =
                                    ReadoutListUiState(
                                        selectedSimulator = Simulator.LmuWindows,
                                        items = ReadoutListItemType.defaultOrder(Simulator.LmuWindows),
                                        selectedItem = null,
                                    ),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = singlePaneDirective,
                                detailContent = { item -> Text("Detail: $item") },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }
}
