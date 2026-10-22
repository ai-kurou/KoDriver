package kurou.kodriver.data

import kurou.kodriver.domain.model.ACE_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.GT7_PS5_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutItemKeyRegistryIntegrationTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerReadoutItemKeys()
    }

    @Test
    fun `全simulator登録後の全keyが一意で復元できqueue defaultsと整合する`() {
        val entries = ReadoutItemKey.entries

        assertEquals(57, entries.size)
        assertEquals(LMU_WINDOWS_READOUT_ITEM_KEYS + GT7_PS5_READOUT_ITEM_KEYS + ACE_WINDOWS_READOUT_ITEM_KEYS, entries)
        assertEquals(entries.size, entries.map { it.value }.distinct().size)
        entries.forEach { key -> assertEquals(key, ReadoutItemKey.fromValue(key.value)) }
        assertEquals(
            entries.filterIsInstance<ReadoutItemKey.TopLevel>().filter { it.supportsQueue }.toSet(),
            QUEUE_ENABLED_STATE_DEFAULT.keys,
        )
    }
}
