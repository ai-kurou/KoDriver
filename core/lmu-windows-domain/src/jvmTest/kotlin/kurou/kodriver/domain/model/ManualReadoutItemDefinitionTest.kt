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

    @Test
    fun `eventKeyは省略するとキー自身になり指定するとその値になる`() {
        val default =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                ManualReadoutItemCategory.VIRTUAL_ENERGY,
                "name",
                "text",
                "{p}",
                { emptyList() },
                { it },
            )
        val overridden =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                ManualReadoutItemCategory.VIRTUAL_ENERGY,
                "name",
                "text",
                "{p}",
                { emptyList() },
                { it },
                LmuWindowsReadoutItemKey.PitTiming.Root,
            )
        assertEquals(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, default.eventKey)
        assertEquals(LmuWindowsReadoutItemKey.PitTiming.Root, overridden.eventKey)
    }
}
