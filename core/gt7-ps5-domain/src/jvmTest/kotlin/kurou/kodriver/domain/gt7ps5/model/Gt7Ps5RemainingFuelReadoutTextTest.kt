package kurou.kodriver.domain.gt7ps5.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelReadoutTextTest {
    @Test
    fun `既定文言の残量パーセントを置換する`() {
        assertEquals(
            "燃料は残り30パーセント",
            formatGt7Ps5RemainingFuelReadoutText(
                GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
