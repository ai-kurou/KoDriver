package kurou.kodriver.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class HeatLevelTest {
    @Test
    fun `全ヒートレベルはライトとダークの対応色を返す`() {
        listOf(LightExtendedColorScheme, DarkExtendedColorScheme).forEach { colors ->
            assertEquals(colors.heatCool, heatColor(HeatLevel.COOL, colors))
            assertEquals(colors.heatOk, heatColor(HeatLevel.OK, colors))
            assertEquals(colors.heatWarm, heatColor(HeatLevel.WARM, colors))
            assertEquals(colors.heatHot, heatColor(HeatLevel.HOT, colors))
        }
    }

    @Test
    fun `温度は既定のcoolとhot閾値と渡されたwarm閾値で各境界を分類する`() {
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(-10.0, warmThreshold = 90.0))
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(69.9, warmThreshold = 90.0))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(70.0, warmThreshold = 90.0))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(89.9, warmThreshold = 90.0))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(90.0, warmThreshold = 90.0))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(109.9, warmThreshold = 90.0))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(110.0, warmThreshold = 90.0))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(120.0, warmThreshold = 90.0))
    }

    @Test
    fun `coolとhot閾値を指定すると既定値を上書きして分類する`() {
        fun level(celsius: Double) =
            temperatureHeatLevel(celsius, warmThreshold = 800.0, coolThreshold = 400.0, hotThreshold = 1000.0)

        assertEquals(HeatLevel.COOL, level(399.9))
        assertEquals(HeatLevel.OK, level(400.0))
        assertEquals(HeatLevel.WARM, level(800.0))
        assertEquals(HeatLevel.HOT, level(1000.0))
    }
}
