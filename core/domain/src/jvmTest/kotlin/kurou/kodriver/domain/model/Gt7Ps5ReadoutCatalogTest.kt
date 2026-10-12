package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5ReadoutCatalogTest {
    @Test
    fun `所属キーと既定値と順序をそのまま公開する`() {
        val catalog = Gt7Ps5ReadoutCatalog
        assertEquals(Simulator.Gt7Ps5, catalog.simulator)
        assertEquals(Gt7Ps5ReadoutItemKey.entries, catalog.entries)
        assertEquals(
            catalog.entries.size,
            catalog.entries
                .map { it.value }
                .distinct()
                .size,
        )
        assertEquals(GT7_PS5_READOUT_ENABLED_STATE_DEFAULT, catalog.enabledStateDefaults)
        assertEquals(GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT, catalog.queueEnabledStateDefaults)
        assertEquals(GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT, catalog.startSoundEnabledStateDefaults)
        assertEquals(gt7Ps5DefaultReadoutOrder(), catalog.defaultReadoutOrder)
        val roots = catalog.entries.filterIsInstance<ReadoutItemKey.TopLevel>()
        assertEquals(roots.toSet(), catalog.enabledStateDefaults.keys)
        assertEquals(roots.toSet(), catalog.defaultReadoutOrder.toSet())
        assertEquals(roots.size, catalog.defaultReadoutOrder.size)
    }

    @Test
    fun `保存値を優先し未保存ならシムの既定値またはtrueを返す`() {
        val empty = emptyMap<ReadoutItemKey, Boolean>()
        Gt7Ps5ReadoutCatalog.entries.forEach { key ->
            assertEquals(GT7_PS5_READOUT_ENABLED_STATE_DEFAULT[key] ?: true, empty.gt7Ps5ReadoutEnabled(key))
            assertTrue(mapOf(key to true).gt7Ps5ReadoutEnabled(key))
            assertFalse(mapOf(key to false).gt7Ps5ReadoutEnabled(key))
        }
    }
}
