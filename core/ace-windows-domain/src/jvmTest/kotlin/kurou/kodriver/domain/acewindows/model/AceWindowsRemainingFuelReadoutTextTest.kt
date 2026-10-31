package kurou.kodriver.domain.acewindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelReadoutTextTest {
    @Test
    fun `既定文言の残量パーセントを置換する`() {
        assertEquals(
            "燃料は残り30パーセント",
            formatAceWindowsRemainingFuelReadoutText(
                ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
