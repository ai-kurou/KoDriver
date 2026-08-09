package kurou.kodriver.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VehicleApproachStartReadoutTypeTest {
    @Test
    fun `fromId は一致する種別を返す`() {
        assertEquals(
            VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
            VehicleApproachStartReadoutType.fromId("left_right_approach"),
        )
    }

    @Test
    fun `fromId は未知の ID のとき null を返す`() {
        assertNull(VehicleApproachStartReadoutType.fromId("unknown"))
    }
}
