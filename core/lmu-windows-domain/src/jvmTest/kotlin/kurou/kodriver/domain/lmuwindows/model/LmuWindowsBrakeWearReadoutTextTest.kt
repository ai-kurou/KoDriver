package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeWearReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "ブレーキ残量30%以下",
            formatLmuWindowsBrakeWearReadoutText(
                LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `既定文言に未知のトークンはない`() {
        assertEquals(
            emptyList(),
            findUnknownLmuWindowsBrakeWearReadoutPlaceholders(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT),
        )
    }
}
