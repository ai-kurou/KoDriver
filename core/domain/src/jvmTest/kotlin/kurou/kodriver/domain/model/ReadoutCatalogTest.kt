package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReadoutCatalogTest {
    private object KnownKey : ReadoutItemKey {
        override val value: String = "known"
    }

    private object UnknownKey : ReadoutItemKey {
        override val value: String = "unknown"
    }

    private val simulatorCatalog =
        object : SimulatorReadoutCatalog {
            override val simulator: Simulator = Simulator.Gt7Ps5
            override val entries: List<ReadoutItemKey> = listOf(KnownKey)
            override val enabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(KnownKey to false)
            override val defaultReadoutOrder: List<ReadoutItemKey> = listOf(KnownKey)
            override val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(KnownKey to true)
            override val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(KnownKey to false)
        }
    private val catalog = ReadoutCatalog(listOf(simulatorCatalog, AceWindowsReadoutCatalog))

    @Test
    fun `カタログ順に結合し安定値から復元する`() {
        assertEquals(listOf(KnownKey) + AceWindowsReadoutItemKey.entries, catalog.entries)
        catalog.entries.forEach { assertEquals(it, catalog.fromValue(it.value)) }
        assertNull(catalog.fromValue("missing"))
        assertEquals(simulatorCatalog.enabledStateDefaults, catalog.enabledStateDefaults(Simulator.Gt7Ps5))
        assertEquals(simulatorCatalog.defaultReadoutOrder, catalog.defaultReadoutOrder(Simulator.Gt7Ps5))
        assertEquals(emptyMap(), catalog.enabledStateDefaults(Simulator.LmuWindows))
        assertEquals(emptyList(), catalog.defaultReadoutOrder(Simulator.LmuWindows))
        assertEquals(true, catalog.queueEnabledStateDefaults[KnownKey])
        assertEquals(false, catalog.startSoundEnabledStateDefaults[KnownKey])
    }

    @Test
    fun `保存値と既定値と未定義項目の順で解決する`() {
        with(catalog) {
            assertTrue(mapOf<ReadoutItemKey, Boolean>(KnownKey to true).readoutEnabled(KnownKey))
            assertFalse(mapOf<ReadoutItemKey, Boolean>(UnknownKey to false).readoutEnabled(UnknownKey))
            assertFalse(emptyMap<ReadoutItemKey, Boolean>().readoutEnabled(KnownKey))
            assertTrue(emptyMap<ReadoutItemKey, Boolean>().readoutEnabled(UnknownKey))
        }
    }

    @Test
    fun `空の構成はキーと既定値と順序を持たない`() {
        val emptyCatalog = ReadoutCatalog(emptyList())
        assertEquals(emptyList(), emptyCatalog.entries)
        assertNull(emptyCatalog.fromValue(KnownKey.value))
        assertEquals(emptyMap(), emptyCatalog.enabledStateDefaults(Simulator.Gt7Ps5))
        assertEquals(emptyList(), emptyCatalog.defaultReadoutOrder(Simulator.Gt7Ps5))
        assertEquals(emptyMap(), emptyCatalog.queueEnabledStateDefaults)
        assertEquals(emptyMap(), emptyCatalog.startSoundEnabledStateDefaults)
    }
}
