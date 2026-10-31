package kurou.kodriver.domain.acewindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelLapsReadoutTextTest {
    @Test
    fun `既定文言の周回数を置換する`() {
        assertEquals(
            "燃料は残り約30周",
            formatAceWindowsRemainingFuelLapsReadoutText(
                ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
