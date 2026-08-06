package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentTest {
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
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() =
        composeScreenshotTest {
            var backEnabled = false
            var githubRepositoryOpened = false
            var releasePageOpened = false
            var themeDialogOpened = false
            var keepScreenOn = true
            var dynamicColorEnabled = false
            var capturedOnBack: (() -> Unit)? = null
            var selectedItem by mutableStateOf<OtherListItemType?>(null)

            setContent {
                OtherContent(
                    uiState =
                        OtherListUiState(
                            selectedItem = selectedItem,
                            keepScreenOn = keepScreenOn,
                            dynamicColorEnabled = dynamicColorEnabled,
                            items = OtherListUiState().items + OtherListItemType.DynamicColor,
                            appVersionLabel = "Windows版KoDriverバージョン",
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
                    detailContent = {
                        item: OtherListItemType,
                        _: Boolean,
                        _: () -> Unit,
                        ->
                        Text("Detail: ${item.id}")
                    },
                )
            }

            assertFalse(backEnabled)

            // ConsoleIp（Desktop では ServerIp・KeepScreenOn が除外されるため最初のアイテム）
            onNode(hasText("ゲーム機・SimHubへ接続するIPアドレス")).performClick()
            waitForIdle()

            onNodeWithText("Detail: console_ip").assertExists()
            assertTrue(backEnabled)

            runOnIdle { capturedOnBack?.invoke() }
            waitUntil { !backEnabled }

            // Volume
            onNode(hasText("音量")).performClick()
            waitForIdle()

            onNodeWithText("Detail: volume").assertExists()
            assertTrue(backEnabled)

            runOnIdle { capturedOnBack?.invoke() }
            waitUntil { !backEnabled }

            // ReadoutStartSound（ダイアログを開く）
            onNode(hasText("読み上げ開始音")).performClick()
            waitForIdle()

            assertFalse(backEnabled)

            // DynamicColor（Switchで直接切り替える）
            onNode(hasText("ダイナミックカラー")).performClick()
            waitForIdle()

            assertTrue(dynamicColorEnabled)
            assertFalse(backEnabled)

            // Theme（ダイアログを開く）
            onNode(hasText("テーマ")).performClick()
            waitForIdle()

            assertTrue(themeDialogOpened)
            assertFalse(backEnabled)

            // GitHubRepository
            onNode(hasText("GitHubレポジトリ")).performClick()
            waitForIdle()

            assertTrue(githubRepositoryOpened)
            assertFalse(backEnabled)

            // ReleasePage
            onNode(hasText("リリースページ")).performClick()
            waitForIdle()

            assertTrue(releasePageOpened)
            assertFalse(backEnabled)

            // License（詳細あり）
            onNode(hasText("ライセンス")).performClick()
            waitForIdle()

            assertTrue(backEnabled)

            runOnIdle { capturedOnBack?.invoke() }
            waitUntil { !backEnabled }

            assertFalse(backEnabled)

            // アプリバージョンを5回連続タップ（onAppVersionTapped経由でDebugStateの詳細ペインへ遷移）
            onNode(hasScrollAction()).performScrollToNode(hasText("Windows版KoDriverバージョン"))
            repeat(5) {
                onNode(hasText("Windows版KoDriverバージョン")).performClick()
                waitForIdle()
            }

            onNodeWithText("Detail: debug_state").assertExists()
            assertTrue(backEnabled)

            runOnIdle { capturedOnBack?.invoke() }
            waitUntil { !backEnabled }

            assertFalse(backEnabled)
        }
}
