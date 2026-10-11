package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureFormatterTest {
    @Test
    fun `摂氏の読み取り値を小数第1位に丸める`() {
        assertEquals("65.2", formatCelsius(CelsiusReading(65.24f)))
        assertEquals("85.4", formatCelsius(CelsiusReading(85.36f)))
    }

    @Test
    fun `燃料の表示値を小数第1位に丸める`() {
        assertEquals("57.1", formatOneDecimal(4000.0 / 70.0))
        assertEquals("10.0", formatOneDecimal(10.0))
    }
}
