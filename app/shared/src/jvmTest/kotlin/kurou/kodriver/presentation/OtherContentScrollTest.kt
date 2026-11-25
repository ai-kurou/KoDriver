package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.item_license
import kurou.kodriver.feature.otherlist.generated.resources.item_volume
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentScrollTest {
    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(400f, 800f)

    private val singlePaneDirective =
        PaneScaffoldDirective(
            maxHorizontalPartitions = 1,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        )

    /** 高さ240dpの一覧に収まらず、スクロール位置を確かめられる項目。 */
    private val scrollableItems =
        listOf(
            OtherListItemType.Volume,
            OtherListItemType.Voice,
            OtherListItemType.VoiceSpeed,
            OtherListItemType.VoicePitch,
            OtherListItemType.ServerIp,
            OtherListItemType.ConsoleIp,
            OtherListItemType.License,
        )

    @Test
    fun `詳細ペインから戻ると一覧のスクロール位置を保つ`() {
        assertScrollPositionKeptAfterDetail(scrollToTopRequest = 0)
    }

    @Test
    fun `scrollToTopRequestが既に正の値でも詳細ペインから戻ると一覧は最上部へ戻らない`() {
        assertScrollPositionKeptAfterDetail(scrollToTopRequest = 1)
    }

    private fun assertScrollPositionKeptAfterDetail(scrollToTopRequest: Int) {
        var selectedItem by mutableStateOf<OtherListItemType?>(null)
        var capturedOnBack: (() -> Unit)? = null
        var firstItemText = ""
        var lastItemText = ""
        rule.setContent {
            firstItemText = stringResource(Res.string.item_volume)
            lastItemText = stringResource(Res.string.item_license)
            AppTheme {
                Box(modifier = Modifier.height(240.dp)) {
                    OtherContent(
                        uiState = OtherListUiState(items = scrollableItems, selectedItem = selectedItem),
                        onItemSelected = { selectedItem = it },
                        onClearSelectedItem = { selectedItem = null },
                        scrollToTopRequest = scrollToTopRequest,
                        scaffoldDirective = singlePaneDirective,
                        windowSizeClass = compactWindowSizeClass,
                        backHandler = { _, _, onBack -> capturedOnBack = onBack },
                        detailContent = { _, _, _, _, _, _ -> Text("詳細スロット") },
                    )
                }
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.onNodeWithText(lastItemText).performClick()
        rule.onNodeWithText("詳細スロット").assertIsDisplayed()
        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitForIdle()

        rule.onNodeWithText("詳細スロット").assertDoesNotExist()
        rule.onNodeWithText(lastItemText).assertIsDisplayed()
        rule.onNodeWithText(firstItemText).assertDoesNotExist()
    }

    @Test
    fun `scrollToTopRequestが増えると一覧を先頭へ戻す`() {
        var scrollToTopRequest by mutableIntStateOf(0)
        var firstItemText = ""
        var lastItemText = ""
        rule.setContent {
            firstItemText = stringResource(Res.string.item_volume)
            lastItemText = stringResource(Res.string.item_license)
            AppTheme {
                Box(modifier = Modifier.height(240.dp)) {
                    OtherContent(
                        uiState = OtherListUiState(items = scrollableItems),
                        onItemSelected = {},
                        onClearSelectedItem = {},
                        scrollToTopRequest = scrollToTopRequest,
                        scaffoldDirective = singlePaneDirective,
                        windowSizeClass = compactWindowSizeClass,
                    )
                }
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.onNodeWithText(firstItemText).assertDoesNotExist()
        rule.runOnIdle { scrollToTopRequest++ }
        rule.waitForIdle()

        rule.onNodeWithText(firstItemText).assertIsDisplayed()
    }
}
