package kurou.kodriver.domain.acewindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsBrakeTemperatureReadoutTextTest {
    @Test
    fun `過熱の既定文言は温度プレースホルダーを含み温度に置換する`() {
        val template = ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
        assertEquals("ブレーキ温度は{celsius}℃以上", template)
        assertEquals("ブレーキ温度は800℃以上", formatAceWindowsBrakeTemperatureReadoutText(template, 800))
        assertEquals(emptyList(), findUnknownAceWindowsBrakeTemperatureReadoutPlaceholders(template))
    }

    @Test
    fun `未知のプレースホルダーは重複なく返しそのまま残す`() {
        val template = "{wheel}{celsius}{x}{wheel}"
        assertEquals("{wheel}800{x}{wheel}", formatAceWindowsBrakeTemperatureReadoutText(template, 800))
        assertEquals(listOf("{wheel}", "{x}"), findUnknownAceWindowsBrakeTemperatureReadoutPlaceholders(template))
    }
}
