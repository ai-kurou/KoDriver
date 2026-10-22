package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemDefinitionTest {
    @Test
    fun `定義ヘルパーはキー値を保持してマップを順序通りに作る`() {
        val first =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                "first",
                "text",
            )
        val second =
            manualReadoutItemDefinitionOf(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                "second",
                "text2",
            )
        assertEquals(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value, first.key)
        assertEquals(
            linkedMapOf(first.key to first, second.key to second),
            manualReadoutItemDefinitionMapOf(first, second),
        )
    }
}
