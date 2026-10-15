package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        rule.onNodeWithText("セット 1").performClick()
        rule.onNodeWithText("詳細 target").performClick()
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("セット 1").assertExists()
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
        rule.onNodeWithText("セット 1").performTouchInput { longClick() }
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("削除").performClick()
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertExists()
        rule.runOnIdle { assertEquals(listOf("target"), deleted) }
    }
}
