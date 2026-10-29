package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureFormatterTest {
    @Test
    fun `カーカス温度は対象ホイールが存在する場合は摂氏をそのまま表示し存在しない場合はハイフンを表示する`() {
        val wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(92.5f))

        assertEquals("92.5", wheelCarcassTemperatureText(wheels, WheelIndex.FRONT_LEFT))
        assertEquals("-", wheelCarcassTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `摂氏の読み取り値を小数第1位に丸める`() {
        assertEquals("65.2", formatCelsius(CelsiusReading(65.24f)))
        assertEquals("85.4", formatCelsius(CelsiusReading(85.36f)))
    }
}
