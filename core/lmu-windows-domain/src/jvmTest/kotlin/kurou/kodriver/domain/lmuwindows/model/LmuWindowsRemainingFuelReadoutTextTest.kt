package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsRemainingFuelReadoutTextTest {
    @Test fun `既定文言と未知トークンを扱う`() {
        assertEquals(
            "燃料は残り30パーセント",
            formatLmuWindowsRemainingFuelReadoutText(LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT, 30),
        )
        assertEquals(listOf("{lap}"), findUnknownLmuWindowsRemainingFuelReadoutPlaceholders("{percent}{lap}{lap}"))
    }
}
