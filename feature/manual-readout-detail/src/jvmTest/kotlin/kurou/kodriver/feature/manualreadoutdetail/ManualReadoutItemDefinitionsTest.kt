package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualReadoutItemDefinitionsTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @Test
    fun `simulator別定義をレジストリ登録済みのSimulator順に集約し未知IDを空で扱う`() {
        assertEquals(
            Simulator.entries.map { it.id },
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
    fun `全simulatorの項目キーは重複せず表示名が空でない`() {
        val definitionKeys = manualReadoutItemDefinitions.values.flatMap { it.keys }
        assertEquals(definitionKeys.size, definitionKeys.toSet().size)
        manualReadoutItemDefinitions.forEach { (simulatorId, definitions) ->
            definitions.keys.forEach { key ->
                assertTrue(manualReadoutItemName(simulatorId, key).isNotBlank(), "$simulatorId/$key")
            }
        }
    }
}
