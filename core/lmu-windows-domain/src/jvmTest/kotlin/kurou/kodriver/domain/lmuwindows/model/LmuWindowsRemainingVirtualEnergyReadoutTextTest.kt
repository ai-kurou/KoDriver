package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsRemainingVirtualEnergyReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "バーチャルエネルギー残量30%以下",
            formatLmuWindowsRemainingVirtualEnergyReadoutText(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
