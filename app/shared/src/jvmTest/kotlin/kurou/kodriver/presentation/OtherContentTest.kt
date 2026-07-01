package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

    private val singlePaneDirective = PaneScaffoldDirective(
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
        var exitConfirmationEnabled = true
        var capturedOnBack: (() -> Unit)? = null
        var selectedItem by mutableStateOf<OtherListItemType?>(null)

        rule.setContent {
            OtherContent(
                uiState = OtherListUiState(
                    selectedItem = selectedItem,
                    exitConfirmationEnabled = exitConfirmationEnabled,
                ),
                onItemSelected = { selectedItem = it },
                onOpenGitHubRepository = { githubRepositoryOpened = true },
                onOpenReleasePage = { releasePageOpened = true },
                onExitConfirmationEnabledChange = { exitConfirmationEnabled = it },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled: Boolean, onBack: () -> Unit ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit -> Text("Detail: ${item.id}") },
            )
        }

        assertFalse(backEnabled)

        // ConsoleIp（Desktop では ServerIp・KeepScreenOn が除外されるため最初のアイテム）
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

        // ReadoutStartSound（ダイアログを開く）
        rule.onNode(hasText("読み上げ開始音")).performClick()
        rule.waitForIdle()

        assertFalse(backEnabled)

        // ExitConfirmation（Switchで直接切り替える）
        rule.onNode(hasText("終了確認を表示")).performClick()
        rule.waitForIdle()

        assertFalse(exitConfirmationEnabled)
        assertFalse(backEnabled)

        // GitHubRepository
        rule.onNode(hasText("GitHubレポジトリ")).performClick()
        rule.waitForIdle()

        assertTrue(githubRepositoryOpened)
        assertFalse(backEnabled)

        // ReleasePage
        rule.onNode(hasText("リリースページ")).performClick()
        rule.waitForIdle()

        assertTrue(releasePageOpened)
        assertFalse(backEnabled)

        // License（詳細あり）
        rule.onNode(hasText("ライセンス")).performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
    }
}
