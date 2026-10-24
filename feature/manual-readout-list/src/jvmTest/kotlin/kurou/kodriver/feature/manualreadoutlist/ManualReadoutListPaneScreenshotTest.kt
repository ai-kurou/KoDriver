package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Test

class ManualReadoutListPaneScreenshotTest {
    @Test
    fun `空状態`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState = ManualReadoutListUiState(canReadSets = true),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `保存済みセット一覧`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState =
                                    ManualReadoutListUiState(
                                        canReadSets = true,
                                        sets =
                                            listOf(
                                                ManualReadoutSet(id = "set-1", hotkeyVirtualKeyCode = 0x41),
                                                ManualReadoutSet(id = "set-2", itemKeys = listOf("SESSION", "FLAG")),
                                            ),
                                    ),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                                showHotkeyChip = true,
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `セットの長押し削除メニュー`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState =
                                    ManualReadoutListUiState(
                                        sets = listOf(ManualReadoutSet(id = "set-1", name = "Fuel & Tyres")),
                                    ),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                            )
                        }
                    }
                }
            }

            onNodeWithText("Fuel & Tyres").performTouchInput { longClick() }
            onNodeWithText("削除").assertIsDisplayed()
            onNode(isPopup()).captureRoboImage()
        }

    @Test
    fun `ヘルプボトムシートホットキーあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 400.dp)) {
                            ManualReadoutHintSheetContent(showHotkeyHint = true)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `ヘルプボトムシートホットキーなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 400.dp)) {
                            ManualReadoutHintSheetContent(showHotkeyHint = false)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `削除確認ダイアログ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        ManualReadoutListDeleteConfirmDialog(onConfirm = {}, onDismiss = {})
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
