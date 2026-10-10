package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatLevelTest {
    @Test
    fun `温度の各境界の直前と境界値を分類する`() {
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(-10.0))
        assertEquals(HeatLevel.COOL, temperatureHeatLevel(69.9))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(70.0))
        assertEquals(HeatLevel.OK, temperatureHeatLevel(94.9))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(95.0))
        assertEquals(HeatLevel.WARM, temperatureHeatLevel(109.9))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(110.0))
        assertEquals(HeatLevel.HOT, temperatureHeatLevel(120.0))
    }

    @Test
    fun `ブレーキはLMUの警告温度を使って各境界を分類する`() {
        assertEquals(HeatLevel.COOL, brakeTemperatureHeatLevel(399.9))
        assertEquals(HeatLevel.OK, brakeTemperatureHeatLevel(400.0))
        assertEquals(HeatLevel.OK, brakeTemperatureHeatLevel(799.9))
        assertEquals(HeatLevel.WARM, brakeTemperatureHeatLevel(800.0))
        assertEquals(HeatLevel.WARM, brakeTemperatureHeatLevel(999.9))
        assertEquals(HeatLevel.HOT, brakeTemperatureHeatLevel(1000.0))
    }

    @Test
    fun `残溝は少ないほど警告色になり境界値を含めて分類する`() {
        assertEquals(HeatLevel.HOT, wearHeatLevel(0.0))
        assertEquals(HeatLevel.HOT, wearHeatLevel(29.9))
        assertEquals(HeatLevel.WARM, wearHeatLevel(30.0))
        assertEquals(HeatLevel.WARM, wearHeatLevel(74.9))
        assertEquals(HeatLevel.OK, wearHeatLevel(75.0))
        assertEquals(HeatLevel.OK, wearHeatLevel(100.0))
    }

    @Test
    fun `タイヤ内部温度の警告境界はACEが90度でLMUが95度でGT7が100度`() {
        assertEquals(HeatLevel.OK, tyreTemperatureHeatLevel(89.9, Simulator.AceWindows))
        assertEquals(HeatLevel.WARM, tyreTemperatureHeatLevel(90.0, Simulator.AceWindows))
        assertEquals(HeatLevel.OK, tyreTemperatureHeatLevel(94.9, Simulator.LmuWindows))
        assertEquals(HeatLevel.WARM, tyreTemperatureHeatLevel(95.0, Simulator.LmuWindows))
        assertEquals(HeatLevel.OK, tyreTemperatureHeatLevel(99.9, Simulator.Gt7Ps5))
        assertEquals(HeatLevel.WARM, tyreTemperatureHeatLevel(100.0, Simulator.Gt7Ps5))
    }
}
