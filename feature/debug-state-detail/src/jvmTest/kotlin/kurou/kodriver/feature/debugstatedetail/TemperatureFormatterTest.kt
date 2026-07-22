package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureFormatterTest {

    @Test
    fun `対象ホイールが存在する場合はケルビンを摂氏に変換して表示する`() {
        val wheels = mapOf(
            WheelIndex.FRONT_LEFT to sampleWheel(surfaceTemperatureK = 358.15),
        )

        assertEquals("85.0", wheelTemperatureText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelTemperatureText(emptyMap(), WheelIndex.FRONT_LEFT))
    }

    private fun sampleWheel(surfaceTemperatureK: Double) = LmuWindowsTyreWheelData(
        surfaceTemperatureK = surfaceTemperatureK,
        carcassTemperatureK = 0.0,
        brakeTemperatureC = 0.0,
        pressureKpa = 0.0,
        wear = 0.0,
    )
}
