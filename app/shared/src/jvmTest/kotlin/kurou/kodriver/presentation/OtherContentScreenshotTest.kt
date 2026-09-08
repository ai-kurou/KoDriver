package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.buildlogic.screenshottest.singlePaneDirective
import kurou.kodriver.buildlogic.screenshottest.twoPaneDirective
import kurou.kodriver.feature.debugstatedetail.DebugStateDetailPaneContent
import kurou.kodriver.feature.debugstatedetail.DebugStateDetailUiState
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPaneContent
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailUiState
import kurou.kodriver.feature.otherfeedbackdetail.OtherFeedbackDetailPaneContent
import kurou.kodriver.feature.otherfeedbackdetail.OtherFeedbackDetailUiState
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPaneContent
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailUiState
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentScreenshotTest {
    // Windows版KoDriverへ接続するIPアドレスは Android 専用項目のため JVM では表示しない。

    @Test
    fun `ゲーム機とSimHubへ接続するIPアドレス詳細を表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.ConsoleIp),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.ConsoleIp) {
                                        OtherConsoleIpDetailPaneContent(
                                            uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `音量詳細を表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.Volume),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.Volume) {
                                        OtherVolumeDetailPaneContent(
                                            uiState = OtherVolumeDetailUiState(volume = 80),
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `フィードバックを送信を表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.Feedback),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.Feedback) {
                                        OtherFeedbackDetailPaneContent(
                                            uiState = OtherFeedbackDetailUiState(),
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `ライセンスを表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.License),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.License) {
                                        OtherLicenseDetailPane(
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `DebugStateDetailを表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.DebugState),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = twoPaneDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.DebugState) {
                                        DebugStateDetailPaneContent(
                                            uiState = DebugStateDetailUiState(),
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
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
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = null),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = singlePaneDirective,
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `折りたたみ端末の展開状態では縦ヒンジを避けて一覧・詳細の両ペインを表示`() =
        composeScreenshotTest {
            setContent {
                AppTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            val density = LocalDensity.current
                            val foldedVerticalHingeDirective =
                                remember(density) {
                                    calculatePaneScaffoldDirective(
                                        WindowAdaptiveInfo(
                                            windowSizeClass = WindowSizeClass.compute(840f, 640f),
                                            windowPosture =
                                                Posture(
                                                    hingeList =
                                                        listOf(
                                                            HingeInfo(
                                                                bounds =
                                                                    with(density) {
                                                                        Rect(
                                                                            left = 410.dp.toPx(),
                                                                            top = 0f,
                                                                            right = 430.dp.toPx(),
                                                                            bottom = 640.dp.toPx(),
                                                                        )
                                                                    },
                                                                isFlat = true,
                                                                isVertical = true,
                                                                isSeparating = true,
                                                                isOccluding = false,
                                                            ),
                                                        ),
                                                ),
                                        ),
                                    )
                                }
                            OtherContent(
                                uiState = OtherListUiState(selectedItem = OtherListItemType.Volume),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = foldedVerticalHingeDirective,
                                detailContent = { itemType, canNavigateBack, onBack, _, _ ->
                                    if (itemType == OtherListItemType.Volume) {
                                        OtherVolumeDetailPaneContent(
                                            uiState = OtherVolumeDetailUiState(volume = 80),
                                            canNavigateBack = canNavigateBack,
                                            onBack = onBack,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }
}
