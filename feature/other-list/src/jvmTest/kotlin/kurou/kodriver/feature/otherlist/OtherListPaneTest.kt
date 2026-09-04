package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeHapticFeedback : HapticFeedback {
    val performedTypes = mutableListOf<HapticFeedbackType>()

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        performedTypes += hapticFeedbackType
    }
}

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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("テレメトリ受信中は画面をスリープさせない")).performClick()

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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("テレメトリ受信中は画面をスリープさせない")).performClick()

        assertEquals(true, keepScreenOn)
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
                onDynamicColorEnabledChange = { dynamicColorEnabled = it },
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("ダイナミックカラー")).performClick()

        assertEquals(false, dynamicColorEnabled)
    }

    @Test
    fun `ハプティックフィードバックをクリックすると切り替えコールバックを呼ぶ`() {
        var hapticFeedbackEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.HapticFeedback),
                        hapticFeedbackEnabled = true,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = { hapticFeedbackEnabled = it },
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("ハプティックフィードバック")).performClick()

        assertEquals(false, hapticFeedbackEnabled)
    }

    @Test
    fun `PC起動時に自動起動をクリックすると切り替えコールバックを呼ぶ`() {
        var startupEnabled: Boolean? = null

        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Startup),
                        startupEnabled = false,
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = { startupEnabled = it },
            )
        }

        rule.onNode(hasText("PC起動時に自動起動")).performClick()

        assertEquals(true, startupEnabled)
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
                                OtherListItemType.DynamicColor,
                                OtherListItemType.HapticFeedback,
                                OtherListItemType.Startup,
                            ),
                    ),
                onItemClick = { clickedItem = it },
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNode(hasText("テレメトリ受信中は画面をスリープさせない")).performClick()
        rule.onNode(hasText("ダイナミックカラー")).performClick()
        rule.onNode(hasText("ハプティックフィードバック")).performClick()
        rule.onNode(hasText("PC起動時に自動起動")).performClick()

        assertNull(clickedItem)
    }

    @Test
    fun `画面スリープ無効のSwitch本体を直接タップするとハプティックフィードバックを発生させる`() {
        val haptic = FakeHapticFeedback()

        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.KeepScreenOn),
                            keepScreenOn = true,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
                )
            }
        }

        rule.onNode(isToggleable()).performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
    }

    @Test
    fun `ダイナミックカラーのSwitch本体を直接タップするとハプティックフィードバックを発生させる`() {
        val haptic = FakeHapticFeedback()

        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.DynamicColor),
                            dynamicColorEnabled = true,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
                )
            }
        }

        rule.onNode(isToggleable()).performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
    }

    @Test
    fun `PC起動時に自動起動のSwitch本体を直接タップするとハプティックフィードバックを発生させる`() {
        val haptic = FakeHapticFeedback()

        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                OtherListPane(
                    uiState =
                        OtherListUiState(
                            items = listOf(OtherListItemType.Startup),
                            startupEnabled = false,
                        ),
                    onItemClick = {},
                    onKeepScreenOnChange = {},
                    onDynamicColorEnabledChange = {},
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
                )
            }
        }

        rule.onNode(isToggleable()).performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                                OtherListItemType.AccessLocalNetworkPermission,
                                OtherListItemType.ConsoleIp,
                                OtherListItemType.Volume,
                                OtherListItemType.Theme,
                                OtherListItemType.License,
                            ),
                    ),
                onItemClick = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
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
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("アプリ設定").assertCountEquals(1)
        rule.onAllNodesWithText("テーマ").assertCountEquals(1)
    }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() {
        var scrollToTopRequest by mutableIntStateOf(0)

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
                    onHapticFeedbackEnabledChange = {},
                    onStartupEnabledChange = {},
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
