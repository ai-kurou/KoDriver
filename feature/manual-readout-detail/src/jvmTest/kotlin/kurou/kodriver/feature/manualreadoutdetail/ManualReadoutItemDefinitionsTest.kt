package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutItemDefinitionsTest {
    @Test
    fun `simulator別定義をLMU ACE GT7順に集約し未知IDを空で扱う`() {
        assertEquals(
            listOf(Simulator.LmuWindows.id, Simulator.AceWindows.id, Simulator.Gt7Ps5.id),
            manualReadoutItemDefinitions.keys.toList(),
        )
        manualReadoutItemDefinitions.forEach { (simulatorId, definitions) ->
            assertEquals(definitions.keys.toList(), manualReadoutItemKeys(simulatorId))
            definitions.values.forEach { definition ->
                assertEquals(definition, manualReadoutItemDefinition(simulatorId, definition.key))
                assertEquals(definition.name, manualReadoutItemName(simulatorId, definition.key))
                assertEquals(definition.textDefault, manualReadoutItemTextDefault(simulatorId, definition.key))
            }
        }
        assertEquals(emptyList(), manualReadoutItemKeys("unknown"))
        assertEquals(null, manualReadoutItemDefinition("unknown", "unknown"))
        assertEquals("", manualReadoutItemName("unknown", "unknown"))
        assertEquals("", manualReadoutItemTextDefault("unknown", "unknown"))
    }

    @Test
    fun `項目表示定義のキーは全simulatorの項目定義のキーと過不足なく一致する`() {
        val definitionKeys = manualReadoutItemDefinitions.values.flatMap { it.keys }
        assertEquals(definitionKeys.size, definitionKeys.toSet().size)
        assertEquals(definitionKeys.toSet(), itemPresentations.keys)
    }
}
