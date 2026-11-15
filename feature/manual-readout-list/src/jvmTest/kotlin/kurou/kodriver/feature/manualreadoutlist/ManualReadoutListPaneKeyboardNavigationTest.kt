package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPaneKeyboardNavigationTest {
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
                                playButtonLayoutEnabled = false,
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

    @Test
    fun `下キーで次の行のカードへフォーカスが移る`() {
        setPane(listOf("行1", "行2", "行3"))
        rule.onNodeWithText("行1").requestFocus()

        press("行1", Key.DirectionDown)

        rule.onNodeWithText("行2").assertIsFocused()
    }

    @Test
    fun `上キーで前の行のカードへフォーカスが移る`() {
        setPane(listOf("行1", "行2", "行3"))
        rule.onNodeWithText("行2").requestFocus()

        press("行2", Key.DirectionUp)

        rule.onNodeWithText("行1").assertIsFocused()
    }

    @Test
    fun `先頭行で上キーを押してもフォーカスは動かず折り返さない`() {
        setPane(listOf("行1", "行2"))
        rule.onNodeWithText("行1").requestFocus()

        press("行1", Key.DirectionUp)

        rule.onNodeWithText("行1").assertIsFocused()
    }

    @Test
    fun `末尾行で下キーを押してもフォーカスは動かず折り返さない`() {
        setPane(listOf("行1", "行2"))
        rule.onNodeWithText("行2").requestFocus()

        press("行2", Key.DirectionDown)

        rule.onNodeWithText("行2").assertIsFocused()
    }

    @Test
    fun `再生ボタンにフォーカスがあっても下キーで次の行のカードへ移る`() {
        setPane(listOf("行1", "行2"))
        rule.onNodeWithContentDescription("行1 を読み上げ").requestFocus()

        press("行1", Key.DirectionDown)

        rule.onNodeWithText("行2").assertIsFocused()
    }

    @Test
    fun `画面外の行へも下キーの連打でフォーカスが移る`() {
        val names = listOf("行1", "行2", "行3", "行4", "行5", "行6")
        setPane(names, height = 300)
        rule.onNodeWithText("行1").requestFocus()

        names.dropLast(1).forEach { press(it, Key.DirectionDown) }

        rule.onNodeWithText("行6").assertIsFocused()
    }

    @Test
    fun `Alt Ctrl Meta Shift付きの下キーでは行を移動しない`() {
        setPane(listOf("行1", "行2"))
        rule.onNodeWithText("行1").requestFocus()

        listOf(Key.AltLeft, Key.CtrlLeft, Key.MetaLeft, Key.ShiftLeft).forEach { modifier ->
            rule.onNodeWithText("行1").performKeyInput {
                keyDown(modifier)
                pressKey(Key.DirectionDown)
                keyUp(modifier)
            }
        }

        rule.onNodeWithText("行1").assertIsFocused()
    }
}
