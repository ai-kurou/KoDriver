package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatLevelTest {
    @Test
    fun `温度の各境界の直前と境界値を分類する`() {
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(-10.0))
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(69.9))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(70.0))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(89.9))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(90.0))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(109.9))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(110.0))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(120.0))
    }
}
