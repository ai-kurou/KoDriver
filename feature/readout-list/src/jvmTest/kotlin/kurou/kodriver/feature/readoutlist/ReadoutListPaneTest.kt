package kurou.kodriver.feature.readoutlist

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `シミュレーター未選択の場合は選択ヒントだけを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                        ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onNodeWithContentDescription("シミュレータを選択").assertIsDisplayed()
        rule.onNodeWithText("読み上げ優先順位").assertDoesNotExist()
    }

    @Test
    fun `シミュレーターを選択するとonSimulatorSelectedが呼ばれる`() {
        val selected = mutableListOf<Simulator>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                        ),
                    onSimulatorSelected = { selected += it },
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onNodeWithContentDescription("シミュレータを選択").performClick()
        rule.onNodeWithText("Le Mans Ultimate（Windows版）").performClick()

        assertEquals(Simulator.LmuWindows, selected.single())
    }

    @Test
    fun `読み上げ項目をタップするとonItemClickが呼ばれる`() {
        val clicked = mutableListOf<ReadoutItemKey>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows),
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                            readoutEnabledStates =
                                mapOf(
                                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                                ),
                        ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onItemClick = { clicked += it },
                )
            }
        }

        rule.onNodeWithContentDescription("バーチャルエナジー残量").performClick()

        assertEquals(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root, clicked.single())
    }

    @Test
    fun `スイッチとキュー追加トグルはON_OFF変更コールバックを呼ぶ`() {
        val readoutChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows),
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { item, enabled -> readoutChanges += item to enabled },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onItemClick = {},
                )
            }
        }

        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(1)
        rule.onAllNodes(hasSwitchRole()).assertCountEquals(1)
        rule.onAllNodes(hasQueueToggleRole())[0].assertIsEnabled().performClick()
        rule.onAllNodes(hasSwitchRole())[0].assertIsEnabled().performClick()

        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to true, queueChanges.single())
        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to false, readoutChanges.single())
    }

    @Test
    fun `スイッチとキュー追加トグルの外側タップ領域は項目タップではなくON_OFF変更コールバックを呼ぶ`() {
        val readoutChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val clicked = mutableListOf<ReadoutItemKey>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows),
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { item, enabled -> readoutChanges += item to enabled },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onItemClick = { clicked += it },
                )
            }
        }

        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()
        rule
            .onNodeWithTag("readoutListSwitchTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()

        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to true, queueChanges.single())
        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to false, readoutChanges.single())
        assertEquals(emptyList(), clicked)
    }

    @Test
    fun `読み上げOFFの項目はキュー追加トグルを無効にする`() {
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            simulators = listOf(Simulator.LmuWindows),
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onItemClick = {},
                )
            }
        }

        rule.onAllNodes(hasQueueToggleRole())[0].assertIsNotEnabled().performClick()

        assertFalse(queueChanges.contains(ReadoutItemKey.LmuWindows.Flag.Root to true))
    }

    private fun hasSwitchRole(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

    private fun hasQueueToggleRole(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
}
