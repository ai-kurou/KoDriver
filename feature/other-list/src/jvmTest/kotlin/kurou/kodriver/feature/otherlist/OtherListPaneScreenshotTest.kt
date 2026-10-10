package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class OtherListPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            OtherListPane(
                                uiState = OtherListUiState(),
                                onItemClick = {},
                                onOverlayVisibleChange = {},
                                onKeepScreenOnChange = {},
                                onDynamicColorEnabledChange = {},
                                onHapticFeedbackEnabledChange = {},
                                onStartupEnabledChange = {},
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `接続先IPアドレスを表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            OtherListPane(
                                uiState =
                                    OtherListUiState(
                                        items = listOf(OtherListItemType.ServerIp),
                                        serverIp = "192.168.1.100",
                                    ),
                                onItemClick = {},
                                onOverlayVisibleChange = {},
                                onKeepScreenOnChange = {},
                                onDynamicColorEnabledChange = {},
                                onHapticFeedbackEnabledChange = {},
                                onStartupEnabledChange = {},
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `アップデートバッジを表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            OtherListPane(
                                uiState = OtherListUiState(hasAppUpdate = true),
                                onItemClick = {},
                                onOverlayVisibleChange = {},
                                onKeepScreenOnChange = {},
                                onDynamicColorEnabledChange = {},
                                onHapticFeedbackEnabledChange = {},
                                onStartupEnabledChange = {},
                            )
                        }
                    }
                }
            }

            onNode(hasScrollAction()).performScrollToNode(hasText("リリースページ"))
            onRoot().captureRoboImage()
        }

    @Test
    fun `振動機能がない端末ではハプティックフィードバック項目が表示されない`() =
        composeScreenshotTest {
            // Desktop向けのbuildOtherListItems()は元々HapticFeedbackとAndroid専用TTS案内を含まないため、
            // AndroidのOS別フィルタ適用後の項目セットを起点にする。
            val itemsIncludingHapticFeedback =
                (OtherListItemType.entries - OtherListItemType.WindowsSpeechUnavailable).filterNot {
                    it == OtherListItemType.DebugState || it == OtherListItemType.Startup
                }
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            OtherListPane(
                                uiState =
                                    OtherListUiState(
                                        items =
                                            itemsIncludingHapticFeedback.filterNot {
                                                it == OtherListItemType.HapticFeedback
                                            },
                                    ),
                                onItemClick = {},
                                onOverlayVisibleChange = {},
                                onKeepScreenOnChange = {},
                                onDynamicColorEnabledChange = {},
                                onHapticFeedbackEnabledChange = {},
                                onStartupEnabledChange = {},
                            )
                        }
                    }
                }
            }

            onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
            onRoot().captureRoboImage()
        }
}
