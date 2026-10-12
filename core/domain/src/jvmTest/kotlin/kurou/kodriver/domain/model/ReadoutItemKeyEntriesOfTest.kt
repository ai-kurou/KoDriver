package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadoutItemKeyEntriesOfTest {
    @Test
    fun `各シミュレーターの候補は型で分離され全キーを過不足なく含む`() {
        val groups = Simulator.entries.map { simulator -> ReadoutItemKey.entriesOf(simulator) }
        Simulator.entries.zip(groups).forEach { (simulator, keys) ->
            assertTrue(keys.isNotEmpty())
            assertTrue(
                keys.all { key ->
                    when (simulator) {
                        Simulator.LmuWindows -> key is LmuWindowsReadoutItemKey
                        Simulator.Gt7Ps5 -> key is Gt7Ps5ReadoutItemKey
                        Simulator.AceWindows -> key is AceWindowsReadoutItemKey
                    }
                },
            )
            assertEquals(ReadoutItemKey.entries.filter { it in keys }, keys)
        }
        assertEquals(ReadoutItemKey.entries.toSet(), groups.flatten().toSet())
        assertEquals(ReadoutItemKey.entries.size, groups.flatten().size)
    }
}
