package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

/** 再生ボタン表示（2列グリッド）での矢印キー移動。セットは左上から右へ、行ごとに並ぶ。 */
class ManualReadoutListPaneGridKeyboardNavigationTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    private fun setPane(
        names: List<String>,
        height: Int = 900,
    ) {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(480.dp, height.dp)) {
                    ManualReadoutListPaneContent(
                        uiState =
                            ManualReadoutListUiState(
                                playButtonLayoutEnabled = true,
                                canReadSets = true,
                                sets = names.map { ManualReadoutSet(id = "id-$it", name = it) },
                            ),
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        onTogglePlayButtonLayout = {},
                        showHotkeyChip = false,
                    )
                }
            }
        }
    }

    private fun press(
        name: String,
        key: Key,
    ) {
        rule.onNodeWithText(name).performKeyInput { pressKey(key) }
    }

    private val addTile = "セットを追加"

    @Test
    fun `右キーで同じ行の右のタイルへ移り左キーで戻る`() {
        setPane(listOf("A", "B", "C", "D"))
        rule.onNodeWithText("A").requestFocus()

        press("A", Key.DirectionRight)
        rule.onNodeWithText("B").assertIsFocused()

        press("B", Key.DirectionLeft)
        rule.onNodeWithText("A").assertIsFocused()
    }

    @Test
    fun `下キーで同じ列の下のタイルへ移り上キーで戻る`() {
        setPane(listOf("A", "B", "C", "D"))
        rule.onNodeWithText("B").requestFocus()

        press("B", Key.DirectionDown)
        rule.onNodeWithText("D").assertIsFocused()

        press("D", Key.DirectionUp)
        rule.onNodeWithText("B").assertIsFocused()
    }

    @Test
    fun `行頭の左キーと行末の右キーと先頭行の上キーではフォーカスが動かず折り返さない`() {
        setPane(listOf("A", "B"))
        rule.onNodeWithText("A").requestFocus()
        press("A", Key.DirectionLeft)
        press("A", Key.DirectionUp)
        rule.onNodeWithText("A").assertIsFocused()

        rule.onNodeWithText("B").requestFocus()
        press("B", Key.DirectionRight)
        rule.onNodeWithText("B").assertIsFocused()
    }

    @Test
    fun `セットを追加タイルも矢印キーの移動先になる`() {
        setPane(listOf("A", "B", "C"))
        rule.onNodeWithText("C").requestFocus()

        press("C", Key.DirectionRight)
        rule.onNodeWithText(addTile).assertIsFocused()

        press(addTile, Key.DirectionUp)
        rule.onNodeWithText("B").assertIsFocused()

        press("B", Key.DirectionDown)
        rule.onNodeWithText(addTile).assertIsFocused()

        press(addTile, Key.DirectionLeft)
        rule.onNodeWithText("C").assertIsFocused()
    }

    @Test
    fun `最終行が欠けていて真下がないときは下キーで最後のタイルへ移る`() {
        // [A B] [C D] [追加] で D の真下はないが、最終行に追加タイルがあるため最後のタイルへ移る。
        setPane(listOf("A", "B", "C", "D"))
        rule.onNodeWithText("D").requestFocus()

        press("D", Key.DirectionDown)

        rule.onNodeWithText(addTile).assertIsFocused()
    }

    @Test
    fun `矢印以外のキーは消費せず既定のフォーカス移動に任せる`() {
        setPane(listOf("A", "B"))
        rule.onNodeWithText("A").requestFocus()

        press("A", Key.Tab)

        rule.onNodeWithText("A").assertIsNotFocused()
    }

    @Test
    fun `画面外のタイルへも下キーの連打でフォーカスが移る`() {
        val names = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        setPane(names, height = 300)
        rule.onNodeWithText("A").requestFocus()

        listOf("A", "C", "E", "G").forEach { press(it, Key.DirectionDown) }

        rule.onNodeWithText(addTile).assertIsFocused()
    }

    @Test
    fun `Alt Ctrl Meta Shift付きの矢印キーでは移動しない`() {
        setPane(listOf("A", "B", "C", "D"))
        rule.onNodeWithText("A").requestFocus()

        listOf(Key.AltLeft, Key.CtrlLeft, Key.MetaLeft, Key.ShiftLeft).forEach { modifier ->
            rule.onNodeWithText("A").performKeyInput {
                keyDown(modifier)
                pressKey(Key.DirectionRight)
                pressKey(Key.DirectionDown)
                keyUp(modifier)
            }
        }

        rule.onNodeWithText("A").assertIsFocused()
    }
}
