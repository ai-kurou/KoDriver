package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreWearReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "タイヤ残存率30%以下",
            formatLmuWindowsTyreWearReadoutText(
                LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `既定文言に未知のトークンはない`() {
        assertEquals(
            emptyList(),
            findUnknownLmuWindowsTyreWearReadoutPlaceholders(LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT),
        )
    }
}
