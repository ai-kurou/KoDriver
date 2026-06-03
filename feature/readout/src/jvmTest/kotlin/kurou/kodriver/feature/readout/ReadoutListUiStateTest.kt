package kurou.kodriver.feature.readout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListUiStateTest {

    @Test
    fun `デフォルトコンストラクタで全フィールドがデフォルト値になる`() {
        val state = ReadoutListUiState()

        assertNull(state.selectedSimulator)
        assertEquals(emptyList(), state.simulators)
        assertEquals(emptyList(), state.items)
        assertEquals(emptyMap(), state.readoutEnabledStates)
    }

    @Test
    fun `シミュレータリストを指定して生成できる`() {
        val state = ReadoutListUiState(
            simulators = listOf("lmu"),
        )

        assertEquals(listOf("lmu"), state.simulators)
        assertNull(state.selectedSimulator)
        assertEquals(emptyList(), state.items)
    }

    @Test
    fun `全フィールドを指定して生成できる`() {
        val state = ReadoutListUiState(
            selectedSimulator = "lmu",
            simulators = listOf("lmu"),
            items = listOf("vehicle_approach", "laps_remaining"),
            readoutEnabledStates = mapOf("vehicle_approach" to true, "laps_remaining" to false),
        )

        assertEquals("lmu", state.selectedSimulator)
        assertEquals(listOf("lmu"), state.simulators)
        assertEquals(listOf("vehicle_approach", "laps_remaining"), state.items)
        assertEquals(mapOf("vehicle_approach" to true, "laps_remaining" to false), state.readoutEnabledStates)
    }

    @Test
    fun `データクラスのコピーが正常に動作する`() {
        val original = ReadoutListUiState(
            selectedSimulator = "lmu",
            simulators = listOf("lmu"),
            items = listOf("vehicle_approach"),
        )
        val copied = original.copy(items = listOf("laps_remaining"))

        assertEquals("lmu", copied.selectedSimulator)
        assertEquals(listOf("lmu"), copied.simulators)
        assertEquals(listOf("laps_remaining"), copied.items)
    }

    @Test
    fun `同じ内容のインスタンスは等価と判定される`() {
        val state1 = ReadoutListUiState(
            selectedSimulator = "lmu",
            simulators = listOf("lmu"),
            items = listOf("vehicle_approach", "laps_remaining"),
            readoutEnabledStates = mapOf("vehicle_approach" to true),
        )
        val state2 = ReadoutListUiState(
            selectedSimulator = "lmu",
            simulators = listOf("lmu"),
            items = listOf("vehicle_approach", "laps_remaining"),
            readoutEnabledStates = mapOf("vehicle_approach" to true),
        )

        assertEquals(state1, state2)
    }

    @Test
    fun `readoutEnabledStatesに存在しないキーはnullを返す`() {
        val state = ReadoutListUiState(
            items = listOf("vehicle_approach", "laps_remaining"),
            readoutEnabledStates = mapOf("vehicle_approach" to true),
        )

        assertEquals(true, state.readoutEnabledStates["vehicle_approach"])
        assertNull(state.readoutEnabledStates["laps_remaining"])
        assertNull(state.readoutEnabledStates["unknown_item"])
    }
}