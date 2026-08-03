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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `通常項目をクリックすると項目クリックコールバックを呼ぶ`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Volume),
                    ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("音量")).performClick()

        assertEquals(OtherListItemType.Volume, clickedItem)
    }

    @Test
    fun `画面スリープ無効をクリックすると切り替えコールバックを呼ぶ`() {
        var keepScreenOn: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.KeepScreenOn),
                        keepScreenOn = true,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = { keepScreenOn = it },
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()

        assertEquals(false, keepScreenOn)
    }

    @Test
    fun `画面スリープ無効がOFFのときにクリックするとONへ切り替えコールバックを呼ぶ`() {
        var keepScreenOn: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.KeepScreenOn),
                        keepScreenOn = false,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = { keepScreenOn = it },
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()

        assertEquals(true, keepScreenOn)
    }

    @Test
    fun `終了確認をクリックすると切り替えコールバックを呼ぶ`() {
        var exitConfirmationEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.ExitConfirmation),
                        exitConfirmationEnabled = true,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = { exitConfirmationEnabled = it },
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("終了確認を表示")).performClick()

        assertEquals(false, exitConfirmationEnabled)
    }

    @Test
    fun `終了確認がOFFのときにクリックするとONへ切り替えコールバックを呼ぶ`() {
        var exitConfirmationEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.ExitConfirmation),
                        exitConfirmationEnabled = false,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = { exitConfirmationEnabled = it },
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("終了確認を表示")).performClick()

        assertEquals(true, exitConfirmationEnabled)
    }

    @Test
    fun `ダイナミックカラーをクリックすると切り替えコールバックを呼ぶ`() {
        var dynamicColorEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.DynamicColor),
                        dynamicColorEnabled = true,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = { dynamicColorEnabled = it },
            )
        }

        rule.onNode(hasText("ダイナミックカラー")).performClick()

        assertEquals(false, dynamicColorEnabled)
    }

    @Test
    fun `Switch項目をクリックしても項目クリックコールバックは呼ばない`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items =
                            listOf(
                                OtherListItemType.KeepScreenOn,
                                OtherListItemType.ExitConfirmation,
                                OtherListItemType.DynamicColor,
                            ),
                    ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("画面をスリープさせない")).performClick()
        rule.onNode(hasText("終了確認を表示")).performClick()
        rule.onNode(hasText("ダイナミックカラー")).performClick()

        assertNull(clickedItem)
    }

    @Test
    fun `アプリバージョンが設定されているとバージョン行を表示する`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = emptyList(),
                        appVersionLabel = "Android版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("Android版KoDriverバージョン").assertCountEquals(1)
        rule.onAllNodesWithText("1.2.3").assertCountEquals(1)
    }

    @Test
    fun `デバッグ状態項目をクリックすると項目クリックコールバックを呼ぶ`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.DebugState),
                    ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("デバッグ状態")).performClick()

        assertEquals(OtherListItemType.DebugState, clickedItem)
    }

    @Test
    fun `フィードバック項目をクリックすると項目クリックコールバックを呼ぶ`() {
        var clickedItem: OtherListItemType? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Feedback),
                    ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onNode(hasText("フィードバックを送信")).performClick()

        assertEquals(OtherListItemType.Feedback, clickedItem)
    }

    @Test
    fun `アプリバージョンを5回連続タップするとコールバックを呼ぶ`() {
        var tappedCount = 0

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = emptyList(),
                        appVersionLabel = "Android版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
                onAppVersionTapped = { tappedCount++ },
            )
        }

        repeat(5) {
            rule.onNode(hasText("Android版KoDriverバージョン")).performClick()
        }

        assertEquals(1, tappedCount)
    }

    @Test
    fun `アプリバージョンを4回タップしてもコールバックを呼ばない`() {
        var tappedCount = 0

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = emptyList(),
                        appVersionLabel = "Android版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
                onAppVersionTapped = { tappedCount++ },
            )
        }

        repeat(4) {
            rule.onNode(hasText("Android版KoDriverバージョン")).performClick()
        }

        assertEquals(0, tappedCount)
    }

    @Test
    fun `表示項目に応じたセクション見出しを表示する`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items =
                            listOf(
                                OtherListItemType.ConsoleIp,
                                OtherListItemType.Volume,
                                OtherListItemType.ExitConfirmation,
                                OtherListItemType.License,
                            ),
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("接続設定").assertCountEquals(1)
        rule.onAllNodesWithText("読み上げ設定").assertCountEquals(1)
        rule.onAllNodesWithText("アプリ設定").assertCountEquals(1)
        rule.onAllNodesWithText("情報").assertCountEquals(1)
    }

    @Test
    fun `項目がないセクション見出しは表示しない`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Volume),
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("接続設定").assertCountEquals(0)
        rule.onAllNodesWithText("読み上げ設定").assertCountEquals(1)
        rule.onAllNodesWithText("アプリ設定").assertCountEquals(0)
        rule.onAllNodesWithText("情報").assertCountEquals(0)
    }

    @Test
    fun `テーマ項目をアプリ設定セクションに表示する`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Theme),
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onExitConfirmationEnabledChange = {},
                onDynamicColorEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("アプリ設定").assertCountEquals(1)
        rule.onAllNodesWithText("テーマ").assertCountEquals(1)
    }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() {
        var scrollToTopRequest by mutableStateOf(0)

        rule.setContent {
            Box(modifier = Modifier.height(160.dp)) {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items =
                                listOf(
                                    OtherListItemType.ConsoleIp,
                                    OtherListItemType.Volume,
                                    OtherListItemType.ReadoutStartSound,
                                    OtherListItemType.ExitConfirmation,
                                    OtherListItemType.Theme,
                                    OtherListItemType.DynamicColor,
                                    OtherListItemType.GitHubRepository,
                                    OtherListItemType.ReleasePage,
                                    OtherListItemType.License,
                                ),
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onExitConfirmationEnabledChange = {},
                    onDynamicColorEnabledChange = {},
                    scrollToTopRequest = scrollToTopRequest,
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
        rule.runOnIdle { scrollToTopRequest++ }

        rule.waitUntil {
            rule.onAllNodesWithText("ゲーム機・SimHubへ接続するIPアドレス").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
