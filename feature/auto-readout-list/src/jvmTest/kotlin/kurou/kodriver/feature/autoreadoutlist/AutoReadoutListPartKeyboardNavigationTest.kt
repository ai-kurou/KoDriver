package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class AutoReadoutListPartKeyboardNavigationTest {
    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    private val flag = LmuWindowsReadoutItemKey.Flag.Root
    private val approach = LmuWindowsReadoutItemKey.VehicleApproach.Root

    private val displayNames = mutableMapOf<ReadoutItemKey, String>()

    private fun setPane(
        items: List<ReadoutItemKey>,
        disabled: Set<ReadoutItemKey> = emptySet(),
    ) {
        rule.setContent {
            KoDriverTheme {
                items.forEach { displayNames[it] = autoReadoutItemDisplayName(it) }
                Box(modifier = Modifier.requiredSize(480.dp, 900.dp)) {
                    AutoReadoutListPane(
                        uiState =
                            AutoReadoutListUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                items = items,
                                autoReadoutEnabledStates = disabled.associateWith { false },
                            ),
                        onMove = { _, _ -> },
                        onReadoutEnabledChanged = { _, _ -> },
                        onQueueEnabledChanged = { _, _ -> },
                        onStartSoundEnabledChanged = { _, _ -> },
                        onItemClick = {},
                    )
                }
            }
        }
    }

    private fun name(item: ReadoutItemKey) = displayNames.getValue(item)

    private fun pressOnRoot(key: Key) {
        rule.onRoot().performKeyInput { pressKey(key) }
    }

    private fun switchOf(item: ReadoutItemKey): SemanticsNodeInteraction {
        val switchTag = "autoReadoutListSwitchTouchTarget:${item.value}"
        return rule.onNode(isToggleable() and hasAnyAncestor(hasTestTag(switchTag)), useUnmergedTree = true)
    }

    private fun startSoundOf(item: ReadoutItemKey) =
        rule.onNodeWithTag("autoReadoutListStartSoundTouchTarget:${item.value}", useUnmergedTree = true)

    @Test
    fun `右キーで主操作領域から読み上げスイッチ、開始音の順に行内の部品へ移る`() {
        setPane(listOf(approach, flag))
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        pressOnRoot(Key.DirectionRight)
        switchOf(approach).assertIsFocused()

        pressOnRoot(Key.DirectionRight)
        startSoundOf(approach).assertIsFocused()
    }

    @Test
    fun `左キーで行内の部品から前の部品へ戻り、主操作領域へも戻れる`() {
        setPane(listOf(approach, flag))
        startSoundOf(approach).requestFocus()

        pressOnRoot(Key.DirectionLeft)
        switchOf(approach).assertIsFocused()

        pressOnRoot(Key.DirectionLeft)
        rule.onNodeWithContentDescription(name(approach)).assertIsFocused()
    }

    @Test
    fun `主操作領域で左キーを押してもフォーカスは動かない`() {
        setPane(listOf(approach, flag))
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        pressOnRoot(Key.DirectionLeft)

        rule.onNodeWithContentDescription(name(approach)).assertIsFocused()
    }

    @Test
    fun `行の最後の部品で右キーを押しても次の行へは移らず折り返さない`() {
        setPane(listOf(approach, flag))
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        repeat(6) { pressOnRoot(Key.DirectionRight) }

        // 行内の部品は主操作領域・スイッチ・開始音（VehicleApproach はキュー非対応）。最後の開始音で止まる。
        startSoundOf(approach).assertIsFocused()
        rule.onNodeWithContentDescription(name(flag)).assertIsNotFocused()
    }

    @Test
    fun `読み上げが無効のときは開始音を飛ばし、スイッチで右キーを押しても動かない`() {
        setPane(listOf(approach, flag), disabled = setOf(approach))
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        pressOnRoot(Key.DirectionRight)
        switchOf(approach).assertIsFocused()

        pressOnRoot(Key.DirectionRight)
        switchOf(approach).assertIsFocused()
    }

    @Test
    fun `行内の部品にいても上下キーは行単位の移動のまま`() {
        setPane(listOf(approach, flag))
        startSoundOf(approach).requestFocus()

        pressOnRoot(Key.DirectionDown)

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `修飾キー付きの左右キーでは行内の部品へ移動しない`() {
        setPane(listOf(approach, flag))
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        rule.onNodeWithContentDescription(name(approach)).performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.DirectionRight)
            keyUp(Key.ShiftLeft)
        }

        rule.onNodeWithContentDescription(name(approach)).assertIsFocused()
    }
}
