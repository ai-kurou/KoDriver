package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenPrimarySimulatorNavItemTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `NavigationBarのLMU (Win)はスペースで2行に折り返す`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                selectedSimulatorId = "lmu_windows",
            )
        }

        assertEquals(2, lineCountOf("LMU (Win)"))
    }

    @Test
    fun `NavigationBarの単語1つのシミュレーター名は1行のまま`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                selectedSimulatorId = "gt7_ps5",
            )
        }

        assertEquals(1, lineCountOf("GT7"))
    }

    @Test
    fun `NavigationRailのLMU (Win)は「(Win)」の前で改行して2行になる`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationRail,
                selectedSimulatorId = "lmu_windows",
            )
        }

        assertEquals(2, lineCountOf("LMU\n(Win)"))
    }

    @Test
    fun `NavigationRailのACE (Win)は「(Win)」の前で改行して2行になる`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationRail,
                selectedSimulatorId = "ace_windows",
            )
        }

        assertEquals(2, lineCountOf("ACE\n(Win)"))
    }

    @Test
    fun `NavigationRailの単語1つのシミュレーター名は1行のまま`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationRail,
                selectedSimulatorId = "gt7_ps5",
            )
        }

        assertEquals(1, lineCountOf("GT7"))
    }

    private fun lineCountOf(label: String): Int {
        val results = mutableListOf<TextLayoutResult>()
        rule.onNode(hasText(label)).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(results)
        }
        return results.single().lineCount
    }
}
