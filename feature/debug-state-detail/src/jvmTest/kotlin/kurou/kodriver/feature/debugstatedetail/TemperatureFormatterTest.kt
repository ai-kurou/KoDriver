package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.core.model.LmuWindowsTyreWheelData
import kurou.kodriver.core.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureFormatterTest {
    @Test
    fun `対象ホイールが存在する場合はケルビンを摂氏に変換して表示する`() {
        val wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to sampleWheel(surfaceTemperatureK = 358.15),
            )

        assertEquals("85.0", wheelTemperatureText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `カーカス温度は対象ホイールが存在する場合は摂氏をそのまま表示する`() {
        val wheels = mapOf(WheelIndex.FRONT_LEFT to 92.5)

        assertEquals("92.5", wheelCarcassTemperatureText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `カーカス温度は対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelCarcassTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    private fun sampleWheel(surfaceTemperatureK: Double) =
        LmuWindowsTyreWheelData(
            surfaceTemperatureK = surfaceTemperatureK,
            carcassTemperatureK = 0.0,
            brakeTemperatureC = 0.0,
            pressureKpa = 0.0,
            wear = 0.0,
        )
}
