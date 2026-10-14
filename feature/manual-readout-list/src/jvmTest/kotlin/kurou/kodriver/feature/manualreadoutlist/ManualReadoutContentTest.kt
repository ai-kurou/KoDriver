package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ManualReadoutContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `行の選択で詳細スロットへIDを渡し戻る操作で一覧へ戻す`() {
        val state = mutableStateOf(ManualReadoutListUiState(sets = listOf(ManualReadoutSet("target"))))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutContentScaffold(
                    uiState = state.value,
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onSetClick = { state.value = state.value.copy(selectedSetId = it) },
                    onClearSelectedSet = { state.value = state.value.copy(selectedSetId = null) },
                    detailContent = { id, onBack ->
                        TextButton(onClick = onBack) { Text("詳細 $id") }
                    },
                )
            }
        }
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("セット 1").performClick()
        rule.onNodeWithText("詳細 target").performClick()
        rule.onNodeWithText("詳細 target").assertDoesNotExist()
        rule.onNodeWithText("セット 1").assertExists()
    }
}
