package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsManualReadoutItem
import kurou.kodriver.domain.model.ManualReadoutValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsManualReadoutItemsTest {
    @Test
    fun `全項目はプレースホルダーを持ち既定文言に含まれプレビューで置換される`() {
        aceWindowsManualReadoutItemDefinitions.values.forEach { definition ->
            assertTrue(definition.placeholder.isNotEmpty())
            assertEquals(emptyList(), definition.findUnknownPlaceholders(definition.placeholder))
            assertTrue(definition.textDefault.contains(definition.placeholder))
            assertFalse(definition.previewFormatter(definition.textDefault).contains(definition.placeholder))
        }
    }

    @Test
    fun `定義はキー順に名前と既定文言を持ち未知キーは空文字`() {
        val definitions = aceWindowsManualReadoutItemDefinitions
        assertEquals(definitions.keys.toList(), aceWindowsManualReadoutItemKeys())
        assertTrue(definitions.isNotEmpty())
        definitions.values.forEach { definition ->
            assertEquals(definition.name, aceWindowsManualReadoutItemName(definition.key))
            assertEquals(definition.textDefault, aceWindowsManualReadoutItemTextDefault(definition.key))
            assertTrue(definition.name.isNotBlank())
            assertTrue(definition.textDefault.isNotBlank())
        }
        assertEquals("", aceWindowsManualReadoutItemName("unknown"))
        assertEquals("", aceWindowsManualReadoutItemTextDefault("unknown"))
    }

    @Test
    fun `手動読み上げ値を保持する`() {
        val item = AceWindowsManualReadoutItem("key", "text", ManualReadoutValue.Percent(42))
        assertEquals(ManualReadoutValue.Percent(42), item.value)
        assertEquals(null, AceWindowsManualReadoutItem("key", "text", null).value)
    }

    @Test
    fun `全項目の開始音キーは項目キー自身`() {
        aceWindowsManualReadoutItemDefinitions.values.forEach { definition ->
            assertEquals(definition.key, definition.eventKey.value)
        }
    }
}
