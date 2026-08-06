package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListPaneTest {
    @Test
    fun `通常項目をクリックすると項目クリックコールバックを呼ぶ`() =
        composeScreenshotTest {
            var clickedItem: OtherListItemType? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.Volume),
                        ),
                    onItemClick = { clickedItem = it },
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("音量")).performClick()

            assertEquals(OtherListItemType.Volume, clickedItem)
        }

    @Test
    fun `画面スリープ無効をクリックすると切り替えコールバックを呼ぶ`() =
        composeScreenshotTest {
            var keepScreenOn: Boolean? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.KeepScreenOn),
                            keepScreenOn = true,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = { keepScreenOn = it },
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("画面をスリープさせない")).performClick()

            assertEquals(false, keepScreenOn)
        }

    @Test
    fun `画面スリープ無効がOFFのときにクリックするとONへ切り替えコールバックを呼ぶ`() =
        composeScreenshotTest {
            var keepScreenOn: Boolean? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.KeepScreenOn),
                            keepScreenOn = false,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = { keepScreenOn = it },
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("画面をスリープさせない")).performClick()

            assertEquals(true, keepScreenOn)
        }

    @Test
    fun `ダイナミックカラーをクリックすると切り替えコールバックを呼ぶ`() =
        composeScreenshotTest {
            var dynamicColorEnabled: Boolean? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.DynamicColor),
                            dynamicColorEnabled = true,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = { dynamicColorEnabled = it },
                )
            }

            onNode(hasText("ダイナミックカラー")).performClick()

            assertEquals(false, dynamicColorEnabled)
        }

    @Test
    fun `Switch項目をクリックしても項目クリックコールバックは呼ばない`() =
        composeScreenshotTest {
            var clickedItem: OtherListItemType? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items =
                                listOf(
                                    OtherListItemType.KeepScreenOn,
                                    OtherListItemType.DynamicColor,
                                ),
                        ),
                    onItemClick = { clickedItem = it },
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("画面をスリープさせない")).performClick()
            onNode(hasText("ダイナミックカラー")).performClick()

            assertNull(clickedItem)
        }

    @Test
    fun `アプリバージョンが設定されているとバージョン行を表示する`() =
        composeScreenshotTest {
            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = emptyList(),
                            appVersionLabel = "Android版KoDriverバージョン",
                            appVersion = "1.2.3",
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onAllNodesWithText("Android版KoDriverバージョン").assertCountEquals(1)
            onAllNodesWithText("1.2.3").assertCountEquals(1)
        }

    @Test
    fun `デバッグ状態項目をクリックすると項目クリックコールバックを呼ぶ`() =
        composeScreenshotTest {
            var clickedItem: OtherListItemType? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.DebugState),
                        ),
                    onItemClick = { clickedItem = it },
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("デバッグ状態")).performClick()

            assertEquals(OtherListItemType.DebugState, clickedItem)
        }

    @Test
    fun `フィードバック項目をクリックすると項目クリックコールバックを呼ぶ`() =
        composeScreenshotTest {
            var clickedItem: OtherListItemType? = null

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.Feedback),
                        ),
                    onItemClick = { clickedItem = it },
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onNode(hasText("フィードバックを送信")).performClick()

            assertEquals(OtherListItemType.Feedback, clickedItem)
        }

    @Test
    fun `アプリバージョンを5回連続タップするとコールバックを呼ぶ`() =
        composeScreenshotTest {
            var tappedCount = 0

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = emptyList(),
                            appVersionLabel = "Android版KoDriverバージョン",
                            appVersion = "1.2.3",
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onAppVersionTapped = { tappedCount++ },
                )
            }

            repeat(5) {
                onNode(hasText("Android版KoDriverバージョン")).performClick()
            }

            assertEquals(1, tappedCount)
        }

    @Test
    fun `アプリバージョンを4回タップしてもコールバックを呼ばない`() =
        composeScreenshotTest {
            var tappedCount = 0

            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = emptyList(),
                            appVersionLabel = "Android版KoDriverバージョン",
                            appVersion = "1.2.3",
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onAppVersionTapped = { tappedCount++ },
                )
            }

            repeat(4) {
                onNode(hasText("Android版KoDriverバージョン")).performClick()
            }

            assertEquals(0, tappedCount)
        }

    @Test
    fun `表示項目に応じたセクション見出しを表示する`() =
        composeScreenshotTest {
            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items =
                                listOf(
                                    OtherListItemType.ConsoleIp,
                                    OtherListItemType.Volume,
                                    OtherListItemType.Theme,
                                    OtherListItemType.License,
                                ),
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onAllNodesWithText("接続設定").assertCountEquals(1)
            onAllNodesWithText("読み上げ設定").assertCountEquals(1)
            onAllNodesWithText("アプリ設定").assertCountEquals(1)
            onAllNodesWithText("情報").assertCountEquals(1)
        }

    @Test
    fun `項目がないセクション見出しは表示しない`() =
        composeScreenshotTest {
            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.Volume),
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onAllNodesWithText("接続設定").assertCountEquals(0)
            onAllNodesWithText("読み上げ設定").assertCountEquals(1)
            onAllNodesWithText("アプリ設定").assertCountEquals(0)
            onAllNodesWithText("情報").assertCountEquals(0)
        }

    @Test
    fun `テーマ項目をアプリ設定セクションに表示する`() =
        composeScreenshotTest {
            setContent {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.Theme),
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                )
            }

            onAllNodesWithText("アプリ設定").assertCountEquals(1)
            onAllNodesWithText("テーマ").assertCountEquals(1)
        }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() =
        composeScreenshotTest {
            var scrollToTopRequest by mutableStateOf(0)

            setContent {
                Box(modifier = Modifier.height(160.dp)) {
                    OtherListPane(
                        uiState =
                            OtherListUiState(
                                items =
                                    listOf(
                                        OtherListItemType.ConsoleIp,
                                        OtherListItemType.Volume,
                                        OtherListItemType.ReadoutStartSound,
                                        OtherListItemType.Theme,
                                        OtherListItemType.DynamicColor,
                                        OtherListItemType.GitHubRepository,
                                        OtherListItemType.ReleasePage,
                                        OtherListItemType.License,
                                    ),
                            ),
                        onItemClick = {},
                        onKeepScreenOnChange = {},
                        onDynamicColorEnabledChange = {},
                        scrollToTopRequest = scrollToTopRequest,
                    )
                }
            }

            onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
            runOnIdle { scrollToTopRequest++ }

            waitUntil {
                onAllNodesWithText("ゲーム機・SimHubへ接続するIPアドレス").fetchSemanticsNodes().isNotEmpty()
            }
        }
}
