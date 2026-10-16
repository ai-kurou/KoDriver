package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatLevelTest {
    @Test
    fun `GT7の閾値の直前と境界で4段階に分類する`() {
        val warm = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble()
        assertEquals(HeatLevel.COOL, tyreTemperatureHeatLevel(69.9))
        assertEquals(HeatLevel.OK, tyreTemperatureHeatLevel(70.0))
        assertEquals(HeatLevel.OK, tyreTemperatureHeatLevel(warm - 0.1))
        assertEquals(HeatLevel.WARM, tyreTemperatureHeatLevel(warm))
        assertEquals(HeatLevel.WARM, tyreTemperatureHeatLevel(109.9))
        assertEquals(HeatLevel.HOT, tyreTemperatureHeatLevel(110.0))
    }
}
