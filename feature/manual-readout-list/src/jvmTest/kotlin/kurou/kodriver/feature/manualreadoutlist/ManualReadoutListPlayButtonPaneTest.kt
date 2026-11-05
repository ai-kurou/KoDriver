package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.height
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPlayButtonPaneTest {
    init {
        registerAllManualReadoutSimulators()
    }

    private val onAddSet: () -> Unit = mockk()
    private val onReadSet: (String) -> Unit = mockk()
    private val onSetClick: (String) -> Unit = mockk()

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `再生ボタン表示の空一覧はFABと空状態を出さず追加タイルから追加する`() {
        every { onAddSet() } returns Unit
        showPlayButtons(sets = emptyList())

        rule.onNodeWithContentDescription("セットを追加").assertDoesNotExist()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertDoesNotExist()
        rule.onNodeWithText("セットを追加").assertIsDisplayed().performClick()
        verify(exactly = 1) { onAddSet() }
        confirmVerified(onAddSet)
    }

    @Test
    fun `再生タイルのタップはセットIDを送信し長押しメニューの編集は詳細を開く`() {
        every { onAddSet() } returns Unit
        every { onReadSet("set-1") } returns Unit
        every { onSetClick("set-1") } returns Unit
        showPlayButtons()

        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsEnabled().performClick()
        rule.onNodeWithText("LMU(Windows)セット1").performSemanticsAction(SemanticsActions.OnLongClick)
        rule.onNodeWithText("編集").performClick()
        rule.onNodeWithContentDescription("セットを追加").assertDoesNotExist()
        rule.onNodeWithText("セットを追加").performClick()
        verify(exactly = 1) { onReadSet("set-1") }
        verify(exactly = 1) { onSetClick("set-1") }
        verify(exactly = 1) { onAddSet() }
        confirmVerified(onReadSet, onSetClick, onAddSet)
    }

    @Test
    fun `読み上げ不可と項目なしのタイルは再生せず長押しメニューから編集できる`() {
        val canReadSets = mutableStateOf(false)
        every { onSetClick("set-1") } returns Unit
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            playButtonLayoutEnabled = true,
                            canReadSets = canReadSets.value,
                            sets =
                                listOf(
                                    ManualReadoutSet("set-1", itemKeys = listOf("SESSION")),
                                    ManualReadoutSet("empty"),
                                ),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = { onReadSet(it) },
                    onSetClick = { onSetClick(it) },
                )
            }
        }
        rule
            .onNodeWithContentDescription("セット 1 を読み上げ")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            .performClick()
        rule.onNodeWithText("LMU(Windows)セット1").performSemanticsAction(SemanticsActions.OnLongClick)
        rule.onNodeWithText("編集").performClick()
        rule.runOnIdle { canReadSets.value = true }
        rule.onNodeWithContentDescription("セット 2 を読み上げ").performClick()
        verify(exactly = 0) { onReadSet("set-1") }
        verify(exactly = 0) { onReadSet("empty") }
        verify(exactly = 1) { onSetClick("set-1") }
        confirmVerified(onReadSet, onSetClick)
    }

    @Test
    fun `再生タイルの長押しメニューは編集と削除を並べ削除は確認後にセットIDを送信する`() {
        every { onSetClick("set-1") } returns Unit
        val deleted = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            playButtonLayoutEnabled = true,
                            canReadSets = true,
                            sets = listOf(ManualReadoutSet("set-1", itemKeys = listOf("SESSION"))),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = { onReadSet(it) },
                    onSetClick = { onSetClick(it) },
                    onDeleteSet = { deleted += it },
                )
            }
        }

        rule.onNodeWithText("LMU(Windows)セット1").performSemanticsAction(SemanticsActions.OnLongClick)
        rule.onNodeWithText("編集").assertIsDisplayed()
        rule.onNodeWithText("削除").assertIsDisplayed().performClick()
        rule.onNodeWithText("セットを削除しますか？").assertIsDisplayed()
        rule.runOnIdle { assertEquals(emptyList<String>(), deleted) }
        rule.onNodeWithText("キャンセル").performClick()
        rule.onNodeWithText("セットを削除しますか？").assertDoesNotExist()
        rule.runOnIdle { assertEquals(emptyList<String>(), deleted) }
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("LMU(Windows)セット1").performSemanticsAction(SemanticsActions.OnLongClick)
        rule.onNodeWithText("削除").performClick()
        val deleteActions = rule.onAllNodesWithText("削除")
        deleteActions.assertCountEquals(1)
        deleteActions[0].performClick()
        rule.runOnIdle { assertEquals(listOf("set-1"), deleted) }
        verify(exactly = 0) { onReadSet("set-1") }
        verify(exactly = 0) { onSetClick("set-1") }
        confirmVerified(onReadSet, onSetClick)
    }

    @Test
    fun `どちらの表示にも編集モードの切替ボタンを出さない`() {
        val playLayout = mutableStateOf(true)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(playButtonLayoutEnabled = playLayout.value),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onTogglePlayButtonLayout = { playLayout.value = !playLayout.value },
                )
            }
        }
        rule.onNodeWithContentDescription("編集モードにする").assertDoesNotExist()
        rule.onNodeWithContentDescription("標準表示").performClick()
        rule.onNodeWithContentDescription("編集モードにする").assertDoesNotExist()
    }

    @Test
    fun `再生ボタン表示は対応環境で割り当て済みのチップだけを表示する`() {
        showPlayButtonHotkeySets(showHotkeyChip = true)
        rule.onNodeWithText("Ctrl+Shift+A", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("未設定", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `再生ボタン表示は非対応環境でチップを表示しない`() {
        showPlayButtonHotkeySets(showHotkeyChip = false)
        rule.onNodeWithText("Ctrl+Shift+A", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText("未設定", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `削除後も標準表示と再生ボタン表示で固定番号を使う`() {
        val first = ManualReadoutSet("first", number = 1)
        val third = ManualReadoutSet("third", itemKeys = listOf("SESSION"), number = 3)
        val state = mutableStateOf(ManualReadoutListUiState(sets = listOf(first, third)))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = state.value,
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        rule.onNodeWithText("LMU(Windows)セット3").assertIsDisplayed()
        rule.runOnIdle { state.value = state.value.copy(sets = listOf(third)) }
        rule.onNodeWithText("LMU(Windows)セット3").assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 3 を読み上げ").assertIsDisplayed()
        rule.runOnIdle { state.value = state.value.copy(playButtonLayoutEnabled = true) }
        rule.onNodeWithText("LMU(Windows)セット3").assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 3 を読み上げ").assertIsDisplayed()
    }

    @Test
    fun `再生タイルはカテゴリドットを集計して表示する`() {
        showPlayButtons(
            sets =
                listOf(
                    ManualReadoutSet(
                        "categories",
                        itemKeys =
                            listOf(
                                LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                                LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
                                "unknown",
                            ),
                    ),
                ),
        )
        rule.onNodeWithContentDescription("燃料 2", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithContentDescription("タイヤ 1", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun showPlayButtons(
        sets: List<ManualReadoutSet> = listOf(ManualReadoutSet("set-1", itemKeys = listOf("SESSION"))),
    ) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            playButtonLayoutEnabled = true,
                            canReadSets = true,
                            sets = sets,
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = { onAddSet() },
                    onReadSet = { onReadSet(it) },
                    onSetClick = { onSetClick(it) },
                )
            }
        }
    }

    @Test
    fun `再生ボタン表示は標準表示への切替セグメントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(playButtonLayoutEnabled = true),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        rule.onNodeWithContentDescription("標準表示").assertIsDisplayed()
    }

    @Test
    fun `追加タイルは同じ行の再生タイルと同じ高さになる`() {
        showPlayButtons()

        val playHeight = rule.onNodeWithContentDescription("セット 1 を読み上げ").getUnclippedBoundsInRoot().height
        val addHeight = rule.onNodeWithText("セットを追加").getUnclippedBoundsInRoot().height
        assertEquals(playHeight, addHeight)
    }

    @Test
    fun `再生タイルが低くなると追加タイルも同じ高さに縮む`() {
        val longName = "とても長いセット名".repeat(8)
        val sets = mutableStateOf(listOf(ManualReadoutSet(id = "set-1", name = longName)))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(playButtonLayoutEnabled = true, sets = sets.value),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        sets.value = listOf(ManualReadoutSet(id = "set-1"))
        rule.waitForIdle()

        val playHeight = rule.onNodeWithContentDescription("セット 1 を読み上げ").getUnclippedBoundsInRoot().height
        val addHeight = rule.onNodeWithText("セットを追加").getUnclippedBoundsInRoot().height
        assertEquals(playHeight, addHeight)
    }

    private fun showPlayButtonHotkeySets(showHotkeyChip: Boolean) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            playButtonLayoutEnabled = true,
                            sets =
                                listOf(
                                    ManualReadoutSet(
                                        id = "assigned",
                                        hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_SHIFT,
                                        hotkeyVirtualKeyCode = 0x41,
                                    ),
                                    ManualReadoutSet(id = "unassigned"),
                                ),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onTogglePlayButtonLayout = {},
                    showHotkeyChip = showHotkeyChip,
                )
            }
        }
    }
}
