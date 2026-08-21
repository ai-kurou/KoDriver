package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureFormatterTest {
    @Test
    fun `対象ホイールが存在する場合は表面温度を摂氏で表示する`() {
        val wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to sampleWheel(surfaceTemperature = CelsiusReading(85.0f)),
            )

        assertEquals("85.0", wheelTemperatureText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `カーカス温度は対象ホイールが存在する場合は摂氏をそのまま表示する`() {
        val wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(92.5f))

        assertEquals("92.5", wheelCarcassTemperatureText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `カーカス温度は対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelCarcassTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    private fun sampleWheel(surfaceTemperature: CelsiusReading) =
        LmuWindowsTyreWheelData(
            surfaceTemperature = surfaceTemperature,
            carcassTemperature = CelsiusReading(0f),
            brakeTemperature = CelsiusReading(0f),
            pressureKpa = PressureKpa(0.0),
            wear = LmuWindowsTyreWearRatio(0.0),
        )
}
