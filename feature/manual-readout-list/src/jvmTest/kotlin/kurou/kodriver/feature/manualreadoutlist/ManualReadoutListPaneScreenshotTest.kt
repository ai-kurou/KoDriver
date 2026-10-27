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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Test

class ManualReadoutListPaneScreenshotTest {
    @Test
    fun `再生ボタン表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState =
                                    ManualReadoutListUiState(
                                        playButtonLayoutEnabled = true,
                                        canReadSets = true,
                                        sets =
                                            listOf(
                                                ManualReadoutSet(
                                                    id = "set-1",
                                                    name = "燃料とタイヤ",
                                                    itemKeys =
                                                        listOf(
                                                            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                                                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                                                        ),
                                                    hotkeyVirtualKeyCode = 0x41,
                                                ),
                                                ManualReadoutSet(id = "set-2"),
                                                ManualReadoutSet(
                                                    id = "set-3",
                                                    itemKeys =
                                                        listOf(
                                                            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                                        ),
                                                ),
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
    fun `再生ボタン表示_編集モード`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPaneContent(
                                uiState =
                                    ManualReadoutListUiState(
                                        playButtonLayoutEnabled = true,
                                        canReadSets = true,
                                        sets =
                                            listOf(
                                                ManualReadoutSet(
                                                    id = "set-1",
                                                    name = "燃料とタイヤ",
                                                    itemKeys =
                                                        listOf(
                                                            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                                                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                                                        ),
                                                    hotkeyVirtualKeyCode = 0x41,
                                                ),
                                                ManualReadoutSet(id = "set-2"),
                                                ManualReadoutSet(
                                                    id = "set-3",
                                                    itemKeys =
                                                        listOf(
                                                            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                                        ),
                                                ),
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
            onNodeWithContentDescription("編集モードにする").performClick()
            onRoot().captureRoboImage()
        }

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
                                onTogglePlayButtonLayout = {},
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
                                                ManualReadoutSet(
                                                    id = "set-2",
                                                    itemKeys =
                                                        listOf(
                                                            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                                                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                                                        ),
                                                ),
                                            ),
                                    ),
                                simulatorId = "lmu_windows",
                                simulatorName = "LMU",
                                onAddSet = {},
                                onReadSet = {},
                                onTogglePlayButtonLayout = {},
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
                                onTogglePlayButtonLayout = {},
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
                        Column(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                            ManualReadoutHintSheetContent(showHotkeyHint = true)
                        }
                    }
                }
            }
            onNodeWithText("ステアリングのボタンから実行する設定を開く").performScrollTo().assertIsDisplayed()
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
