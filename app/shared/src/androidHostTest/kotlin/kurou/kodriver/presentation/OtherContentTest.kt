@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherContentTest {
    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

    private val singlePaneDirective =
        PaneScaffoldDirective(
            maxHorizontalPartitions = 1,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        )

    @Test
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        val state = OtherContentTestState()
        rule.setOtherContent(state)

        assertFalse(state.backEnabled)

        // ServerIp（Android専用項目）
        rule.navigateToDetailAndBack(state, "Windows版KoDriverへ接続するIPアドレス", "Detail: server_ip")

        // ConsoleIp
        rule.navigateToDetailAndBack(state, "ゲーム機・SimHubへ接続するIPアドレス", "Detail: console_ip")

        // Volume
        rule.navigateToDetailAndBack(state, "音量", "Detail: volume")

        rule.toggleSwitchesAndOpenDialogs(state)

        // License（詳細あり）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
        rule.onNode(hasText("ライセンス")).performClick()
        rule.waitForIdle()

        assertTrue(state.backEnabled)

        rule.runOnIdle { state.capturedOnBack?.invoke() }
        rule.waitUntil { !state.backEnabled }

        assertFalse(state.backEnabled)

        // アプリバージョンを5回連続タップ（onAppVersionTapped経由でDebugStateの詳細ペインへ遷移）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Android版KoDriverバージョン"))
        repeat(5) {
            rule.onNode(hasText("Android版KoDriverバージョン")).performClick()
            rule.waitForIdle()
        }

        rule.onNodeWithText("Detail: debug_state").assertExists()
        assertTrue(state.backEnabled)

        rule.runOnIdle { state.capturedOnBack?.invoke() }
        rule.waitUntil { !state.backEnabled }

        assertFalse(state.backEnabled)
    }

    private class OtherContentTestState {
        var backEnabled = false
        var githubRepositoryOpened = false
        var releasePageOpened = false
        var readoutStartSoundDialogOpened = false
        var themeDialogOpened = false
        var keepScreenOn = false
        var dynamicColorEnabled = false
        var hapticFeedbackEnabled = false
        var capturedOnBack: (() -> Unit)? = null
    }

    private fun ComposeContentTestRule.setOtherContent(state: OtherContentTestState) {
        setContent {
            var selectedItem by remember { mutableStateOf<OtherListItemType?>(null) }
            OtherContent(
                uiState =
                    OtherListUiState(
                        selectedItem = selectedItem,
                        keepScreenOn = state.keepScreenOn,
                        dynamicColorEnabled = state.dynamicColorEnabled,
                        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                        appVersionLabel = "Android版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemSelected = { selectedItem = it },
                onOpenGitHubRepository = { state.githubRepositoryOpened = true },
                onOpenReleasePage = { state.releasePageOpened = true },
                onOpenReadoutStartSoundDialog = { state.readoutStartSoundDialogOpened = true },
                onOpenThemeDialog = { state.themeDialogOpened = true },
                onKeepScreenOnChange = { state.keepScreenOn = it },
                onDynamicColorEnabledChange = { state.dynamicColorEnabled = it },
                onHapticFeedbackEnabledChange = { state.hapticFeedbackEnabled = it },
                onAppVersionTapped = { selectedItem = OtherListItemType.DebugState },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled: Boolean, _, onBack: () -> Unit ->
                    state.backEnabled = enabled
                    state.capturedOnBack = onBack
                },
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit, _: Long?, _: Long ->
                    Text("Detail: ${item.id}")
                },
            )
        }
    }

    private fun ComposeContentTestRule.navigateToDetailAndBack(
        state: OtherContentTestState,
        itemText: String,
        expectedDetailText: String,
    ) {
        onNode(hasText(itemText)).performClick()
        waitForIdle()

        onNodeWithText(expectedDetailText).assertExists()
        assertTrue(state.backEnabled)

        runOnIdle { state.capturedOnBack?.invoke() }
        waitUntil { !state.backEnabled }
    }

    private fun ComposeContentTestRule.toggleSwitchesAndOpenDialogs(state: OtherContentTestState) {
        // KeepScreenOn（Android専用トグル。Switchで直接切り替える）
        onNode(hasText("画面をスリープさせない")).performClick()
        waitForIdle()

        assertTrue(state.keepScreenOn)
        assertFalse(state.backEnabled)

        // ReadoutStartSound（ダイアログを開く）
        onNode(hasText("読み上げ開始音")).performClick()
        waitForIdle()

        assertTrue(state.readoutStartSoundDialogOpened)
        assertFalse(state.backEnabled)

        // Theme（ダイアログを開く）
        onNode(hasScrollAction()).performScrollToNode(hasText("テーマ"))
        onNode(hasText("テーマ")).performClick()
        waitForIdle()

        assertTrue(state.themeDialogOpened)
        assertFalse(state.backEnabled)

        // DynamicColor（Switchで直接切り替える）
        onNode(hasScrollAction()).performScrollToNode(hasText("ダイナミックカラー"))
        onNode(hasText("ダイナミックカラー")).performClick()
        waitForIdle()

        assertTrue(state.dynamicColorEnabled)
        assertFalse(state.backEnabled)

        // HapticFeedback（Android専用トグル。Switchで直接切り替える）
        onNode(hasScrollAction()).performScrollToNode(hasText("ハプティックフィードバック"))
        onNode(hasText("ハプティックフィードバック")).performClick()
        waitForIdle()

        assertTrue(state.hapticFeedbackEnabled)
        assertFalse(state.backEnabled)

        // GitHubRepository
        onNode(hasScrollAction()).performScrollToNode(hasText("GitHubレポジトリ"))
        onNode(hasText("GitHubレポジトリ")).performClick()
        waitForIdle()

        assertTrue(state.githubRepositoryOpened)
        assertFalse(state.backEnabled)

        // ReleasePage
        onNode(hasScrollAction()).performScrollToNode(hasText("リリースページ"))
        onNode(hasText("リリースページ")).performClick()
        waitForIdle()

        assertTrue(state.releasePageOpened)
        assertFalse(state.backEnabled)
    }
}
