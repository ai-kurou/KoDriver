package kurou.kodriver.data

import kurou.kodriver.domain.model.AceWindowsReadoutCatalog
import kurou.kodriver.domain.model.Gt7Ps5ReadoutCatalog
import kurou.kodriver.domain.model.LmuWindowsReadoutCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppReadoutCatalogTest {
    @Test
    fun `全シムのキーを重複なく従来の順序で復元する`() {
        val catalogs = listOf(LmuWindowsReadoutCatalog, Gt7Ps5ReadoutCatalog, AceWindowsReadoutCatalog)
        val entries = APP_READOUT_CATALOG.entries
        assertEquals(catalogs.flatMap { it.entries }, entries)
        assertEquals(entries.size, entries.map { it.value }.distinct().size)
        entries.forEach { assertEquals(it, APP_READOUT_CATALOG.fromValue(it.value)) }
        assertNull(APP_READOUT_CATALOG.fromValue("unknown"))
        catalogs.forEach { catalog ->
            assertEquals(catalog.enabledStateDefaults, APP_READOUT_CATALOG.enabledStateDefaults(catalog.simulator))
            assertEquals(catalog.defaultReadoutOrder, APP_READOUT_CATALOG.defaultReadoutOrder(catalog.simulator))
        }
    }
}
