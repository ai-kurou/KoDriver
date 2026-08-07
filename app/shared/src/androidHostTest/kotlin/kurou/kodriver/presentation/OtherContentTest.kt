@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
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
        var backEnabled = false
        var githubRepositoryOpened = false
        var releasePageOpened = false
        var themeDialogOpened = false
        var keepScreenOn = false
        var dynamicColorEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var selectedItem by mutableStateOf<OtherListItemType?>(null)

        rule.setContent {
            OtherContent(
                uiState =
                    OtherListUiState(
                        selectedItem = selectedItem,
                        keepScreenOn = keepScreenOn,
                        dynamicColorEnabled = dynamicColorEnabled,
                        appVersionLabel = "Android版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemSelected = { selectedItem = it },
                onOpenGitHubRepository = { githubRepositoryOpened = true },
                onOpenReleasePage = { releasePageOpened = true },
                onOpenThemeDialog = { themeDialogOpened = true },
                onKeepScreenOnChange = { keepScreenOn = it },
                onDynamicColorEnabledChange = { dynamicColorEnabled = it },
                onAppVersionTapped = { selectedItem = OtherListItemType.DebugState },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled: Boolean, _, onBack: () -> Unit ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit -> Text("Detail: ${item.id}") },
            )
        }

        assertFalse(backEnabled)

        // ServerIp（Android専用項目）
        rule.onNode(hasText("Windows版KoDriverへ接続するIPアドレス")).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Detail: server_ip").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        // ConsoleIp
        rule.onNode(hasText("ゲーム機・SimHubへ接続するIPアドレス")).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Detail: console_ip").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        // Volume
        rule.onNode(hasText("音量")).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Detail: volume").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        // KeepScreenOn（Android専用トグル。Switchで直接切り替える）
        rule.onNode(hasText("画面をスリープさせない")).performClick()
        rule.waitForIdle()

        assertTrue(keepScreenOn)
        assertFalse(backEnabled)

        // ReadoutStartSound（ダイアログを開く）
        rule.onNode(hasText("読み上げ開始音")).performClick()
        rule.waitForIdle()

        assertFalse(backEnabled)

        // Theme（ダイアログを開く）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("テーマ"))
        rule.onNode(hasText("テーマ")).performClick()
        rule.waitForIdle()

        assertTrue(themeDialogOpened)
        assertFalse(backEnabled)

        // DynamicColor（Switchで直接切り替える）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("ダイナミックカラー"))
        rule.onNode(hasText("ダイナミックカラー")).performClick()
        rule.waitForIdle()

        assertTrue(dynamicColorEnabled)
        assertFalse(backEnabled)

        // GitHubRepository
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("GitHubレポジトリ"))
        rule.onNode(hasText("GitHubレポジトリ")).performClick()
        rule.waitForIdle()

        assertTrue(githubRepositoryOpened)
        assertFalse(backEnabled)

        // ReleasePage
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("リリースページ"))
        rule.onNode(hasText("リリースページ")).performClick()
        rule.waitForIdle()

        assertTrue(releasePageOpened)
        assertFalse(backEnabled)

        // License（詳細あり）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("ライセンス"))
        rule.onNode(hasText("ライセンス")).performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)

        // アプリバージョンを5回連続タップ（onAppVersionTapped経由でDebugStateの詳細ペインへ遷移）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Android版KoDriverバージョン"))
        repeat(5) {
            rule.onNode(hasText("Android版KoDriverバージョン")).performClick()
            rule.waitForIdle()
        }

        rule.onNodeWithText("Detail: debug_state").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
    }
}
