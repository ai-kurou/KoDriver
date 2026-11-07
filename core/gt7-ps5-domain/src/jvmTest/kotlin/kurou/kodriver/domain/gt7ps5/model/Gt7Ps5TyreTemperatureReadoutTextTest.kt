package kurou.kodriver.domain.gt7ps5.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5TyreTemperatureReadoutTextTest {
    @Test
    fun `過熱の既定文言は温度プレースホルダーを含み温度に置換する`() {
        val template = GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
        assertEquals("タイヤ過熱{celsius}℃", template)
        assertEquals("タイヤ過熱30℃", formatGt7Ps5TyreTemperatureReadoutText(template, 30))
        assertEquals(emptyList(), findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(template))
    }
}
