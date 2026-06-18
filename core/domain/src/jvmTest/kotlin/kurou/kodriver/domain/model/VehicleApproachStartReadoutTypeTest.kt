package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleApproachStartReadoutTypeTest {

    @Test
    fun `fromId は一致する種別を返す`() {
        assertEquals(
            VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
            VehicleApproachStartReadoutType.fromId("left_right_approach"),
        )
    }

    @Test
    fun `fromId は未知の ID のとき CAR_LEFT_RIGHT を返す`() {
        assertEquals(
            VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
            VehicleApproachStartReadoutType.fromId("unknown"),
        )
    }
}
