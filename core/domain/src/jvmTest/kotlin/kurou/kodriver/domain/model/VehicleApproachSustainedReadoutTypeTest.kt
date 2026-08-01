package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleApproachSustainedReadoutTypeTest {
    @Test
    fun `fromId は一致する種別を返す`() {
        assertEquals(
            VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
            VehicleApproachSustainedReadoutType.fromId("left_right_sustained"),
        )
    }

    @Test
    fun `fromId は未知の ID のとき KEEP_LEFT_RIGHT を返す`() {
        assertEquals(
            VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
            VehicleApproachSustainedReadoutType.fromId("unknown"),
        )
    }
}
