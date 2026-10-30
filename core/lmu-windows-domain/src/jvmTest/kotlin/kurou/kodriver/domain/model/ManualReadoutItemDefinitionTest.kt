package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemDefinitionTest {
    @Test
    fun `定義ヘルパーはキー値を保持してマップを順序通りに作る`() {
        val first =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                ManualReadoutItemCategory.VIRTUAL_ENERGY,
                "first",
                "text",
                "{first}",
                { emptyList() },
                { "preview-first" },
            )
        val second =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                ManualReadoutItemCategory.VIRTUAL_ENERGY,
                "second",
                "text2",
                "{second}",
                { listOf(it) },
                { "preview-second" },
            )
        assertEquals(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value, first.key)
        assertEquals(ManualReadoutItemCategory.VIRTUAL_ENERGY, first.category)
        assertEquals("{first}", first.placeholder)
        assertEquals(emptyList(), first.findUnknownPlaceholders("{x}"))
        assertEquals("preview-first", first.previewFormatter("text"))
        assertEquals(listOf("{x}"), second.findUnknownPlaceholders("{x}"))
        assertEquals("preview-second", second.previewFormatter("text2"))
        assertEquals(
            linkedMapOf(first.key to first, second.key to second),
            manualReadoutItemDefinitionMapOf(first, second),
        )
    }
}
