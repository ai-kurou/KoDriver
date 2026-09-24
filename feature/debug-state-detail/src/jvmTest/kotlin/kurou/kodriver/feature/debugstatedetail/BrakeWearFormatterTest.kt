package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class BrakeWearFormatterTest {
    @Test
    fun `対象ホイールが存在する場合は小数第3位までの値を表示する`() {
        val wheels = mapOf(WheelIndex.FRONT_LEFT to 0.9713)

        assertEquals("0.971", wheelLifeText(wheels, WheelIndex.FRONT_LEFT))
    }

    @Test
    fun `対象ホイールが存在しない場合はハイフンを表示する`() {
        assertEquals("-", wheelLifeText(emptyMap(), WheelIndex.FRONT_LEFT))
    }
}
