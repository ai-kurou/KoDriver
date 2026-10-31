package kurou.kodriver.domain.gt7ps5.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelLapsReadoutTextTest {
    @Test
    fun `既定文言の周回数を置換する`() {
        assertEquals(
            "燃料は残り約30周",
            formatGt7Ps5RemainingFuelLapsReadoutText(
                GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
