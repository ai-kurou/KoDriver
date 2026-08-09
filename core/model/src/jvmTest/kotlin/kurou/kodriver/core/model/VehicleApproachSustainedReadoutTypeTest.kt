package kurou.kodriver.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VehicleApproachSustainedReadoutTypeTest {
    @Test
    fun `fromId は一致する種別を返す`() {
        assertEquals(
            VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
            VehicleApproachSustainedReadoutType.fromId("left_right_sustained"),
        )
    }

    @Test
    fun `fromId は未知の ID のとき null を返す`() {
        assertNull(VehicleApproachSustainedReadoutType.fromId("unknown"))
    }
}
