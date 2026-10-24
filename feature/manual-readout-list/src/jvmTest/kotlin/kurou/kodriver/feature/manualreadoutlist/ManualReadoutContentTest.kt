package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ManualReadoutContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `行の選択で詳細スロットへIDを渡し戻る操作で一覧へ戻す`() {
        val state = mutableStateOf(ManualReadoutListUiState(sets = listOf(ManualReadoutSet("target"))))
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutContentScaffold(
                        uiState = state.value,
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        windowSizeClass = WindowSizeClass.compute(360f, 640f),
                        scaffoldDirective =
                            PaneScaffoldDirective(
                                maxHorizontalPartitions = 1,
                                horizontalPartitionSpacerSize = 0.dp,
                                maxVerticalPartitions = 1,
                                verticalPartitionSpacerSize = 0.dp,
                                defaultPanePreferredWidth = 360.dp,
                                excludedBounds = emptyList(),
                            ),
                        onAddSet = {},
                        onReadSet = {},
                        onSetClick = { state.value = state.value.copy(selectedSetId = it) },
                        onClearSelectedSet = { state.value = state.value.copy(selectedSetId = null) },
                        detailContent = { id, canNavigateBack, onBack ->
                            if (canNavigateBack) {
                                TextButton(onClick = onBack) { Text("詳細 $id") }
                            }
                        },
                    )
                }
            }
        }
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("LMU(Windows)セット1").performClick()
        rule.onNodeWithText("詳細 target").performClick()
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("LMU(Windows)セット1").assertExists()
    }

    @Test
    fun `選択中セットの削除を通知して一覧状態が更新されると詳細を閉じる`() {
        val state =
            mutableStateOf(
                ManualReadoutListUiState(sets = listOf(ManualReadoutSet("target")), selectedSetId = "target"),
            )
        val deleted = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                    ManualReadoutContentScaffold(
                        uiState = state.value,
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        windowSizeClass = WindowSizeClass.compute(840f, 640f),
                        onAddSet = {},
                        onReadSet = {},
                        onSetClick = {},
                        onClearSelectedSet = {},
                        onDeleteSet = {
                            deleted.add(it)
                            state.value = ManualReadoutListUiState()
                        },
                        detailContent = { id, _, _ -> Text("詳細 $id") },
                    )
                }
            }
        }
        rule.onNodeWithText("詳細 target").assertExists()
        rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertExists()
        rule.runOnIdle { assertEquals(listOf("target"), deleted) }
    }

    @Test
    fun `再タップは選択中なら選択解除だけを未選択ならスクロールだけを要求する`() {
        var clearCount = 0
        var scrollCount = 0
        handleManualReadoutTabReselected("target", { clearCount++ }, { scrollCount++ })
        assertEquals(1, clearCount)
        assertEquals(0, scrollCount)
        handleManualReadoutTabReselected(null, { clearCount++ }, { scrollCount++ })
        assertEquals(1, clearCount)
        assertEquals(1, scrollCount)
    }

    @Test
    fun `スクロール要求が増えると一覧を先頭へ戻し同じ要求では位置を維持する`() {
        val request = mutableIntStateOf(0)
        val listState = LazyListState()
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutContentScaffold(
                        uiState = ManualReadoutListUiState(sets = List(40) { ManualReadoutSet("set-$it") }),
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        onSetClick = {},
                        onClearSelectedSet = {},
                        scrollToTopRequest = request.intValue,
                        listState = listState,
                        detailContent = { _, _, _ -> },
                    )
                }
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(25)
        rule.runOnIdle {
            assertEquals(25, listState.firstVisibleItemIndex)
            request.intValue++
        }
        rule.waitForIdle()
        rule.runOnIdle {
            assertEquals(0, listState.firstVisibleItemIndex)
            assertEquals(0, listState.firstVisibleItemScrollOffset)
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(25)
        rule.runOnIdle { assertEquals(25, listState.firstVisibleItemIndex) }
        rule.runOnIdle { request.intValue++ }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(0, listState.firstVisibleItemIndex) }
    }
}
