package kurou.kodriver.feature.readout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemTypeTest {

    @Test
    fun `vehicle_approach は VehicleApproach を返す`() {
        assertEquals(ReadoutItemType.VehicleApproach, ReadoutItemType.fromId("vehicle_approach"))
    }

    @Test
    fun `flag は Flag を返す`() {
        assertEquals(ReadoutItemType.Flag, ReadoutItemType.fromId("flag"))
    }

    @Test
    fun `不明な ID は null を返す`() {
        assertNull(ReadoutItemType.fromId("unknown"))
    }

    @Test
    fun `空文字列は null を返す`() {
        assertNull(ReadoutItemType.fromId(""))
    }
}
