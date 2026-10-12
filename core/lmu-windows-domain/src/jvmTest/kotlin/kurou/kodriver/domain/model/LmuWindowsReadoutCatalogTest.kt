package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsReadoutCatalogTest {
    @Test
    fun `所属キーと既定値と順序をそのまま公開する`() {
        val catalog = LmuWindowsReadoutCatalog
        assertEquals(Simulator.LmuWindows, catalog.simulator)
        assertEquals(LmuWindowsReadoutItemKey.entries, catalog.entries)
        assertEquals(
            catalog.entries.size,
            catalog.entries
                .map { it.value }
                .distinct()
                .size,
        )
        assertEquals(LMU_WINDOWS_READOUT_ENABLED_STATE_DEFAULT, catalog.enabledStateDefaults)
        assertEquals(LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT, catalog.queueEnabledStateDefaults)
        assertEquals(LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT, catalog.startSoundEnabledStateDefaults)
        assertEquals(lmuWindowsDefaultReadoutOrder(), catalog.defaultReadoutOrder)
        val roots = catalog.entries.filterIsInstance<ReadoutItemKey.TopLevel>()
        assertEquals(roots.toSet(), catalog.enabledStateDefaults.keys)
        assertEquals(roots.toSet(), catalog.defaultReadoutOrder.toSet())
        assertEquals(roots.size, catalog.defaultReadoutOrder.size)
    }

    @Test
    fun `保存値を優先し未保存ならシムの既定値またはtrueを返す`() {
        val empty = emptyMap<ReadoutItemKey, Boolean>()
        LmuWindowsReadoutCatalog.entries.forEach { key ->
            assertEquals(LMU_WINDOWS_READOUT_ENABLED_STATE_DEFAULT[key] ?: true, empty.lmuWindowsReadoutEnabled(key))
            assertTrue(mapOf(key to true).lmuWindowsReadoutEnabled(key))
            assertFalse(mapOf(key to false).lmuWindowsReadoutEnabled(key))
        }
    }
}
