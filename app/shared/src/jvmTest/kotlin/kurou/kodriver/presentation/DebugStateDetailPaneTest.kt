package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun TestDebugStateDetailPane(selectedSimulatorId: String) {
        DebugStateDetailPane(
            selectedSimulatorId = selectedSimulatorId,
            canNavigateBack = true,
            onBack = {},
            lmuWindowsPane = { _, _ -> Text(LMU_TEXT) },
            gt7Ps5Pane = { _, _ -> Text(GT7_TEXT) },
            aceWindowsPane = { _, _ -> Text(ACE_TEXT) },
        )
    }

    @Test
    fun `選択中のシミュレーターに対応するペインだけを表示する`() {
        var simulatorId by mutableStateOf("lmu_windows")
        rule.setContent { TestDebugStateDetailPane(simulatorId) }
        mapOf("lmu_windows" to LMU_TEXT, "gt7_ps5" to GT7_TEXT, "ace_windows" to ACE_TEXT).forEach { (id, text) ->
            rule.runOnIdle { simulatorId = id }
            rule.onNodeWithText(text).assertIsDisplayed()
            ALL_TEXTS.filter { it != text }.forEach { rule.onNodeWithText(it).assertDoesNotExist() }
        }
    }

    @Test
    fun `表示中にシミュレーターを切り替えると対応するペインへ入れ替わる`() {
        var simulatorId by mutableStateOf("lmu_windows")
        rule.setContent { TestDebugStateDetailPane(simulatorId) }
        rule.onNodeWithText(LMU_TEXT).assertIsDisplayed()

        rule.runOnIdle { simulatorId = "gt7_ps5" }
        rule.onNodeWithText(GT7_TEXT).assertIsDisplayed()
        rule.onNodeWithText(LMU_TEXT).assertDoesNotExist()

        rule.runOnIdle { simulatorId = "ace_windows" }
        rule.onNodeWithText(ACE_TEXT).assertIsDisplayed()
        rule.onNodeWithText(GT7_TEXT).assertDoesNotExist()

        rule.runOnIdle { simulatorId = "lmu_windows" }
        rule.onNodeWithText(LMU_TEXT).assertIsDisplayed()
        rule.onNodeWithText(ACE_TEXT).assertDoesNotExist()
    }

    @Test
    fun `未知のシミュレーターIDではどのペインも表示しない`() {
        rule.setContent { TestDebugStateDetailPane("unknown") }
        ALL_TEXTS.forEach { rule.onNodeWithText(it).assertDoesNotExist() }
    }

    @Test
    fun `選択中のペインへ戻る操作の状態を渡す`() {
        var received: Pair<Boolean, () -> Unit>? = null
        val onBack = {}
        rule.setContent {
            DebugStateDetailPane(
                selectedSimulatorId = "gt7_ps5",
                canNavigateBack = true,
                onBack = onBack,
                lmuWindowsPane = { _, _ -> },
                gt7Ps5Pane = { canNavigateBack, back -> received = canNavigateBack to back },
                aceWindowsPane = { _, _ -> },
            )
        }
        rule.runOnIdle {
            assertEquals(true, received?.first)
            assertEquals(onBack, received?.second)
        }
    }

    private companion object {
        const val LMU_TEXT = "LMU Pane"
        const val GT7_TEXT = "GT7 Pane"
        const val ACE_TEXT = "ACE Pane"
        val ALL_TEXTS = listOf(LMU_TEXT, GT7_TEXT, ACE_TEXT)
    }
}
