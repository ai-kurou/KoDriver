package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import kotlin.test.assertNull

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
    ) {
        rule.setContent {
            KoDriverTheme {
                items.forEach { displayNames[it] = autoReadoutItemDisplayName(it) }
                Box(modifier = Modifier.requiredSize(480.dp, height.dp)) {
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
    fun `Alt付きの下キーでは行を移動しない`() {
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
    fun `隣の項目インデックスは範囲内だけ返し端では折り返さない`() {
        assertEquals(1, adjacentAutoReadoutItemIndex(current = 0, step = 1, itemCount = 3))
        assertEquals(1, adjacentAutoReadoutItemIndex(current = 2, step = -1, itemCount = 3))
        assertNull(adjacentAutoReadoutItemIndex(current = 0, step = -1, itemCount = 3))
        assertNull(adjacentAutoReadoutItemIndex(current = 2, step = 1, itemCount = 3))
    }

    @Test
    fun `行のフォーカス状態は自分の行が外れたときだけ解除される`() {
        val state = AutoReadoutRowFocusState()
        state.onRowFocusChanged(index = 1, hasFocus = true)
        state.onRowFocusChanged(index = 0, hasFocus = false)
        assertEquals(1, state.focusedItemIndex)

        state.onRowFocusChanged(index = 1, hasFocus = false)
        assertEquals(-1, state.focusedItemIndex)
    }
}
