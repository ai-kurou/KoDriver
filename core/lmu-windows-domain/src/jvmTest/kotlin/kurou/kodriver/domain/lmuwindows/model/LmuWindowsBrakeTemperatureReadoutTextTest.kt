package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeTemperatureReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "ブレーキ温度30℃以上",
            formatLmuWindowsBrakeTemperatureReadoutText(
                LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }
}
