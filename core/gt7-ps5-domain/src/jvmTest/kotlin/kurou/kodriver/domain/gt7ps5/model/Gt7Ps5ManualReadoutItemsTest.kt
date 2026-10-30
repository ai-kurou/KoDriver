package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Gt7Ps5ManualReadoutItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5ManualReadoutItemsTest {
    @Test
    fun `全項目はプレースホルダーを持ち既定文言に含まれプレビューで置換される`() {
        gt7Ps5ManualReadoutItemDefinitions.values.forEach { definition ->
            assertTrue(definition.placeholder.isNotEmpty())
            assertEquals(emptyList(), definition.findUnknownPlaceholders(definition.placeholder))
            assertTrue(definition.textDefault.contains(definition.placeholder))
            assertFalse(definition.previewFormatter(definition.textDefault).contains(definition.placeholder))
        }
    }

    @Test
    fun `定義はキー順に名前と既定文言を持ち未知キーは空文字`() {
        val definitions = gt7Ps5ManualReadoutItemDefinitions
        assertEquals(definitions.keys.toList(), gt7Ps5ManualReadoutItemKeys())
        assertTrue(definitions.isNotEmpty())
        definitions.values.forEach { definition ->
            assertEquals(definition.name, gt7Ps5ManualReadoutItemName(definition.key))
            assertEquals(definition.textDefault, gt7Ps5ManualReadoutItemTextDefault(definition.key))
            assertTrue(definition.name.isNotBlank())
            assertTrue(definition.textDefault.isNotBlank())
        }
        assertEquals("", gt7Ps5ManualReadoutItemName("unknown"))
        assertEquals("", gt7Ps5ManualReadoutItemTextDefault("unknown"))
    }

    @Test
    fun `手動読み上げ値を保持する`() {
        val item = Gt7Ps5ManualReadoutItem("key", "text", 42)
        assertEquals(42, item.value)
        assertEquals(null, Gt7Ps5ManualReadoutItem("key", "text", null).value)
    }
}
