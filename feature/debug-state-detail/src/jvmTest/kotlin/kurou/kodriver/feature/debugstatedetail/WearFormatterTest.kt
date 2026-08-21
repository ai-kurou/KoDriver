package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class WearFormatterTest {
    @Test
    fun `対象ホイールが存在する場合は残タイヤ溝割合をパーセントで表示する`() {
        val wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to sampleWheel(wear = 0.8),
            )

        assertEquals("80.0", wheelWearPercentText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelWearPercentText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    private fun sampleWheel(wear: Double) =
        LmuWindowsTyreWheelData(
            surfaceTemperature = CelsiusReading(0f),
            carcassTemperature = CelsiusReading(0f),
            brakeTemperatureC = 0.0,
            pressureKpa = 0.0,
            wear = LmuWindowsTyreWearRatio(wear),
        )
}
