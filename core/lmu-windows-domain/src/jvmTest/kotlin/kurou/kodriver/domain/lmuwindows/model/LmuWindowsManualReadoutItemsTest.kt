package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsManualReadoutItemsTest {
    @Test
    fun `定義はキー順に名前と既定文言を持ち未知キーは空文字`() {
        val definitions = lmuWindowsManualReadoutItemDefinitions
        assertEquals(definitions.keys.toList(), lmuWindowsManualReadoutItemKeys())
        assertTrue(definitions.isNotEmpty())
        definitions.values.forEach { definition ->
            assertEquals(definition.name, lmuWindowsManualReadoutItemName(definition.key))
            assertEquals(definition.textDefault, lmuWindowsManualReadoutItemTextDefault(definition.key))
            assertTrue(definition.name.isNotBlank())
            assertTrue(definition.textDefault.isNotBlank())
        }
        assertEquals("", lmuWindowsManualReadoutItemName("unknown"))
        assertEquals("", lmuWindowsManualReadoutItemTextDefault("unknown"))
    }

    @Test
    fun `手動読み上げ値と温度判定を保持する`() {
        val item = LmuWindowsManualReadoutItem("key", "text", 0.5, 90f, 123L, 4)
        assertEquals(0.5, item.ratio)
        assertEquals(90f, item.celsius)
        assertEquals(123L, item.lapTimeMs)
        assertEquals(4, item.remainingLaps)
        assertTrue(isLmuWindowsManualReadoutTemperatureItem(LmuWindowsReadoutItemKey.TyreTemperature.Root.value))
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem("unknown"))
    }
}
