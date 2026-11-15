package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
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
import kotlin.test.assertEquals

class AutoReadoutListKeyboardNavigationTest {
    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    private val flag = LmuWindowsReadoutItemKey.Flag.Root
    private val damage = LmuWindowsReadoutItemKey.VehicleDamage.Root
    private val fuel = LmuWindowsReadoutItemKey.RemainingFuel.Root
    private val energy = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
    private val tyreWear = LmuWindowsReadoutItemKey.TyreWear.Root
    private val brakeWear = LmuWindowsReadoutItemKey.BrakeWear.Root

    private val displayNames = mutableMapOf<ReadoutItemKey, String>()

    private fun setPane(
        items: List<ReadoutItemKey>,
        height: Int = 900,
        onMove: (Int, Int) -> Unit = { _, _ -> },
    ) {
        rule.setContent {
            KoDriverTheme {
                items.forEach { displayNames[it] = autoReadoutItemDisplayName(it) }
                Box(modifier = Modifier.requiredSize(480.dp, height.dp)) {
                    AutoReadoutListPane(
                        uiState = AutoReadoutListUiState(selectedSimulator = Simulator.LmuWindows, items = items),
                        onMove = onMove,
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

    private fun press(
        item: ReadoutItemKey,
        key: Key,
    ) {
        rule.onNodeWithContentDescription(name(item)).performKeyInput { pressKey(key) }
    }

    @Test
    fun `下キーで次の行の主操作領域へフォーカスが移る`() {
        setPane(listOf(flag, damage, fuel))
        rule.onNodeWithContentDescription(name(flag)).requestFocus()

        press(flag, Key.DirectionDown)

        rule.onNodeWithContentDescription(name(damage)).assertIsFocused()
    }

    @Test
    fun `上キーで前の行の主操作領域へフォーカスが移る`() {
        setPane(listOf(flag, damage, fuel))
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        press(damage, Key.DirectionUp)

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `先頭行で上キーを押してもフォーカスは動かない`() {
        setPane(listOf(flag, damage))
        rule.onNodeWithContentDescription(name(flag)).requestFocus()

        press(flag, Key.DirectionUp)

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `末尾行で下キーを押してもフォーカスは折り返さない`() {
        setPane(listOf(flag, damage))
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        press(damage, Key.DirectionDown)

        rule.onNodeWithContentDescription(name(damage)).assertIsFocused()
    }

    @Test
    fun `行内のスイッチにフォーカスがあっても下キーで次の行の主操作領域へ移る`() {
        setPane(listOf(flag, damage))
        rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))[0].requestFocus()

        rule
            .onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))[0]
            .performKeyInput { pressKey(Key.DirectionDown) }

        rule.onNodeWithContentDescription(name(damage)).assertIsFocused()
    }

    @Test
    fun `画面外の行へも下キーの連打でフォーカスが移る`() {
        val items = listOf(flag, damage, fuel, energy, tyreWear, brakeWear)
        setPane(items, height = 300)
        rule.onNodeWithContentDescription(name(flag)).requestFocus()

        items.dropLast(1).forEach { press(it, Key.DirectionDown) }

        rule.onNodeWithContentDescription(name(brakeWear)).assertIsFocused()
    }

    @Test
    fun `Alt付きの下キーでは行のフォーカスは次の行へ移らない`() {
        setPane(listOf(flag, damage))
        rule.onNodeWithContentDescription(name(flag)).requestFocus()

        rule.onNodeWithContentDescription(name(flag)).performKeyInput {
            keyDown(Key.AltLeft)
            pressKey(Key.DirectionDown)
            keyUp(Key.AltLeft)
        }

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `並べ替えで行の位置が変わってもフォーカス中の行を基準に移動する`() {
        var items by mutableStateOf(listOf(flag, damage, fuel))
        rule.setContent {
            KoDriverTheme {
                items.forEach { displayNames[it] = autoReadoutItemDisplayName(it) }
                Box(modifier = Modifier.requiredSize(480.dp, 900.dp)) {
                    AutoReadoutListPane(
                        uiState = AutoReadoutListUiState(selectedSimulator = Simulator.LmuWindows, items = items),
                        onMove = { _, _ -> },
                        onReadoutEnabledChanged = { _, _ -> },
                        onQueueEnabledChanged = { _, _ -> },
                        onStartSoundEnabledChanged = { _, _ -> },
                        onItemClick = {},
                    )
                }
            }
        }
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        items = listOf(damage, flag, fuel)
        rule.waitForIdle()
        press(damage, Key.DirectionDown)

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `Ctrl_Meta_Shift付きの下キーや上下以外のキーでは行を移動しない`() {
        setPane(listOf(flag, damage))
        rule.onNodeWithContentDescription(name(flag)).requestFocus()

        listOf(Key.CtrlLeft, Key.MetaLeft, Key.ShiftLeft).forEach { modifier ->
            rule.onNodeWithContentDescription(name(flag)).performKeyInput {
                keyDown(modifier)
                pressKey(Key.DirectionDown)
                keyUp(modifier)
            }
        }
        press(flag, Key.DirectionLeft)
        press(flag, Key.DirectionRight)

        rule.onNodeWithContentDescription(name(flag)).assertIsFocused()
    }

    @Test
    fun `Tab移動でタッチ判定用の空Boxとシェブロンにはフォーカスが止まらない`() {
        val approach = LmuWindowsReadoutItemKey.VehicleApproach.Root
        setPane(listOf(approach))
        val blank = rule.onNodeWithTag("autoReadoutListQueueBlankTouchTarget:${approach.value}", useUnmergedTree = true)
        val chevron = rule.onNodeWithTag("autoReadoutListChevronTouchTarget:${approach.value}", useUnmergedTree = true)
        rule.onNodeWithContentDescription(name(approach)).requestFocus()

        repeat(6) {
            rule.onRoot().performKeyInput { pressKey(Key.Tab) }
            blank.assertIsNotFocused()
            chevron.assertIsNotFocused()
        }
    }

    private fun pressWithModifier(
        item: ReadoutItemKey,
        modifier: Key,
        key: Key,
    ) {
        rule.onNodeWithContentDescription(name(item)).performKeyInput {
            keyDown(modifier)
            pressKey(key)
            keyUp(modifier)
        }
    }

    @Test
    fun `Alt付きの下キーで行の並べ替えが依頼される`() {
        val moves = mutableListOf<Pair<Int, Int>>()
        setPane(listOf(flag, damage, fuel), onMove = { from, to -> moves += from to to })
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        pressWithModifier(damage, Key.AltLeft, Key.DirectionDown)

        assertEquals(listOf(1 to 2), moves)
    }

    @Test
    fun `Alt付きの上キーで行の並べ替えが依頼される`() {
        val moves = mutableListOf<Pair<Int, Int>>()
        setPane(listOf(flag, damage, fuel), onMove = { from, to -> moves += from to to })
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        pressWithModifier(damage, Key.AltRight, Key.DirectionUp)

        assertEquals(listOf(1 to 0), moves)
    }

    @Test
    fun `先頭でAlt付きの上キー、末尾でAlt付きの下キーでは並べ替えが依頼されない`() {
        val moves = mutableListOf<Pair<Int, Int>>()
        setPane(listOf(flag, damage), onMove = { from, to -> moves += from to to })
        rule.onNodeWithContentDescription(name(flag)).requestFocus()
        pressWithModifier(flag, Key.AltLeft, Key.DirectionUp)
        rule.onNodeWithContentDescription(name(damage)).requestFocus()
        pressWithModifier(damage, Key.AltLeft, Key.DirectionDown)

        assertEquals(emptyList(), moves)
    }

    @Test
    fun `Altなしの上下キーやShift付きでは並べ替えが依頼されない`() {
        val moves = mutableListOf<Pair<Int, Int>>()
        setPane(listOf(flag, damage, fuel), onMove = { from, to -> moves += from to to })
        rule.onNodeWithContentDescription(name(damage)).requestFocus()

        press(damage, Key.DirectionDown)
        pressWithModifier(flag, Key.ShiftLeft, Key.DirectionDown)

        assertEquals(emptyList(), moves)
    }

    @Test
    fun `フォーカスが行の外にあるときはAlt付きの下キーで並べ替えが依頼されない`() {
        val moves = mutableListOf<Pair<Int, Int>>()
        setPane(listOf(flag, damage), onMove = { from, to -> moves += from to to })

        rule.onRoot().performKeyInput {
            keyDown(Key.AltLeft)
            pressKey(Key.DirectionDown)
            keyUp(Key.AltLeft)
        }

        assertEquals(emptyList(), moves)
    }
}
