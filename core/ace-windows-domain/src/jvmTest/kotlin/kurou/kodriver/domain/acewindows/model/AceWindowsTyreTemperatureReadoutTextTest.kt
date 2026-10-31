package kurou.kodriver.domain.acewindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsTyreTemperatureReadoutTextTest {
    @Test
    fun `過熱の既定文言は温度プレースホルダーを含み温度に置換する`() {
        val template = ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
        assertEquals("タイヤ過熱 {celsius}度", template)
        assertEquals("タイヤ過熱 30度", formatAceWindowsTyreTemperatureReadoutText(template, 30))
        assertEquals(emptyList(), findUnknownAceWindowsTyreTemperatureReadoutPlaceholders(template))
    }
}
