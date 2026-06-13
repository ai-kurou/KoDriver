package kurou.kodriver.feature.readoutlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeTest {

    @Test
    fun `vehicle_approach は VehicleApproach を返す`() {
        assertEquals(ReadoutListItemType.VehicleApproach, ReadoutListItemType.fromId("vehicle_approach"))
    }

    @Test
    fun `flag は Flag を返す`() {
        assertEquals(ReadoutListItemType.Flag, ReadoutListItemType.fromId("flag"))
    }

    @Test
    fun `vehicle_damage は VehicleDamage を返す`() {
        assertEquals(ReadoutListItemType.VehicleDamage, ReadoutListItemType.fromId("vehicle_damage"))
    }

    @Test
    fun `不明な ID は null を返す`() {
        assertNull(ReadoutListItemType.fromId("unknown"))
    }

    @Test
    fun `空文字列は null を返す`() {
        assertNull(ReadoutListItemType.fromId(""))
    }
}
