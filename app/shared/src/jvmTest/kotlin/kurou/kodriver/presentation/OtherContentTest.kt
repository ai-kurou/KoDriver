package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        var consoleIpDialogOpened = false
        var githubRepositoryOpened = false
        var releasePageOpened = false
        var capturedOnBack: (() -> Unit)? = null
        var selectedItem by mutableStateOf<OtherListItemType?>(null)

        rule.setContent {
            OtherContent(
                uiState = OtherListUiState(selectedItem = selectedItem),
                onItemSelected = { selectedItem = it },
                onOpenGitHubRepository = { githubRepositoryOpened = true },
                onOpenReleasePage = { releasePageOpened = true },
                onOpenConsoleIpDialog = { consoleIpDialogOpened = true },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { item, _, _ -> Text("Detail: ${item.id}") },
            )
        }

        assertFalse(backEnabled)

        // item_0: ConsoleIp (ダイアログを開く、Desktop では ServerIp が含まれないためインデックス 0)
        rule.onNodeWithTag("other_item_0").performClick()
        rule.waitForIdle()

        assertTrue(consoleIpDialogOpened)
        assertFalse(backEnabled)

        // item_1: Volume (詳細あり)
        rule.onNodeWithTag("other_item_1").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Detail: volume").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        // item_2: ReadoutStartSound (ダイアログを開く)
        rule.onNodeWithTag("other_item_2").performClick()
        rule.waitForIdle()

        assertFalse(backEnabled)

        // item_3: GitHubRepository
        rule.onNodeWithTag("other_item_3").performClick()
        rule.waitForIdle()

        assertTrue(githubRepositoryOpened)
        assertFalse(backEnabled)

        // item_4: ReleasePage
        rule.onNodeWithTag("other_item_4").performClick()
        rule.waitForIdle()

        assertTrue(releasePageOpened)
        assertFalse(backEnabled)

        // item_5: License (詳細あり)
        rule.onNodeWithTag("other_item_5").performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
    }
}
