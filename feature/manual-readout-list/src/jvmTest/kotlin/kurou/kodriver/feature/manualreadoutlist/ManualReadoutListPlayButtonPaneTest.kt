package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPlayButtonPaneTest {
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
    fun `再生タイルのタップはセットIDを送信し長押しは詳細を開く`() {
        every { onAddSet() } returns Unit
        every { onReadSet("set-1") } returns Unit
        every { onSetClick("set-1") } returns Unit
        showPlayButtons()

        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsEnabled().performClick()
        rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
        rule.onNodeWithContentDescription("セットを追加").assertDoesNotExist()
        rule.onNodeWithText("セットを追加").performClick()
        verify(exactly = 1) { onReadSet("set-1") }
        verify(exactly = 1) { onSetClick("set-1") }
        verify(exactly = 1) { onAddSet() }
        confirmVerified(onReadSet, onSetClick, onAddSet)
    }

    @Test
    fun `読み上げ不可と項目なしのタイルは再生せず長押し編集できる`() {
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
        rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
        rule.runOnIdle { canReadSets.value = true }
        rule.onNodeWithContentDescription("セット 2 を読み上げ").performClick()
        verify(exactly = 0) { onReadSet("set-1") }
        verify(exactly = 0) { onReadSet("empty") }
        verify(exactly = 1) { onSetClick("set-1") }
        confirmVerified(onReadSet, onSetClick)
    }

    @Test
    fun `鉛筆で編集を開始するとタップと長押しで編集し終了すると再生に戻る`() {
        every { onSetClick("set-1") } returns Unit
        every { onReadSet("set-1") } returns Unit
        showPlayButtons()

        rule.onNodeWithContentDescription("編集モードにする").performClick()
        rule.onNodeWithText("タップで編集").assertIsDisplayed()
        rule.onNodeWithText("LMU(Windows)セット1").performClick()
        rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
        verify(exactly = 2) { onSetClick("set-1") }
        verify(exactly = 0) { onReadSet("set-1") }
        rule.onNodeWithContentDescription("編集モードを終了").performClick()
        rule.onNodeWithText("タップで編集").assertDoesNotExist()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        verify(exactly = 1) { onReadSet("set-1") }
        confirmVerified(onSetClick, onReadSet)
    }

    @Test
    fun `表示切替後は編集モードを解除し標準表示には鉛筆を出さない`() {
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
        rule.onNodeWithContentDescription("編集モードにする").performClick()
        rule.onNodeWithContentDescription("標準表示に切り替え").performClick()
        rule.onNodeWithContentDescription("編集モードにする").assertDoesNotExist()
        rule.onNodeWithContentDescription("編集モードを終了").assertDoesNotExist()
        rule.onNodeWithContentDescription("再生ボタン表示に切り替え").performClick()
        rule.onNodeWithContentDescription("編集モードにする").assertIsDisplayed()
    }

    @Test
    fun `再生ボタン表示は対応環境で割り当て済みのチップだけを表示する`() {
        showPlayButtonHotkeySets(showHotkeyChip = true)
        rule.onNodeWithText("Ctrl+Shift+A", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("未設定", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithContentDescription("編集モードにする").performClick()
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
    fun `再生タイルはカテゴリドットを集計して編集モードでも表示する`() {
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
        rule.onNodeWithContentDescription("編集モードにする").performClick()
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
    fun `再生ボタン表示は標準表示への切替アイコンを表示する`() {
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
        rule.onNodeWithContentDescription("標準表示に切り替え").assertIsDisplayed()
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
