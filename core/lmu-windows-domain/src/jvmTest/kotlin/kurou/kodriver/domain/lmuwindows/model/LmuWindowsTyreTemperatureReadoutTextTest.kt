package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperatureReadoutTextTest {
    @Test
    fun `過熱と低温の既定文言は温度プレースホルダーを含み温度に置換する`() {
        listOf(
            LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to "タイヤ過熱",
            LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT to "タイヤ低温",
        ).forEach { (template, warning) ->
            assertEquals("$warning {celsius}度", template)
            assertEquals("$warning 30度", formatLmuWindowsTyreTemperatureReadoutText(template, 30))
            assertEquals(emptyList(), findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(template))
        }
    }
}
