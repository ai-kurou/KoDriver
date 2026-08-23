package kurou.kodriver.feature.main

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SimulatorSwitcherTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `シミュレータ未選択の場合はプレースホルダーアイコンを表示する`() {
        rule.setContent {
            KoDriverTheme {
                SimulatorSwitcher(
                    selectedSimulator = null,
                    simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                    onSimulatorSelected = {},
                )
            }
        }

        rule.onNodeWithContentDescription("シミュレータを切替").assertExists()
    }

    @Test
    fun `アイコンをタップするとシミュレータ一覧が表示される`() {
        rule.setContent {
            KoDriverTheme {
                SimulatorSwitcher(
                    selectedSimulator = Simulator.LmuWindows,
                    simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                    onSimulatorSelected = {},
                )
            }
        }

        rule.onNodeWithContentDescription("シミュレータを切替").performClick()

        rule.onNodeWithText("Le Mans Ultimate（Windows版）").assertExists()
        rule.onNodeWithText("Gran Turismo 7（PS5）").assertExists()
    }

    @Test
    fun `一覧からシミュレータを選択するとonSimulatorSelectedが呼ばれる`() {
        val selected = mutableListOf<Simulator>()
        rule.setContent {
            KoDriverTheme {
                SimulatorSwitcher(
                    selectedSimulator = Simulator.LmuWindows,
                    simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                    onSimulatorSelected = { selected += it },
                )
            }
        }

        rule.onNodeWithContentDescription("シミュレータを切替").performClick()
        rule.onNodeWithText("Gran Turismo 7（PS5）").performClick()

        assertEquals(Simulator.Gt7Ps5, selected.single())
    }
}
