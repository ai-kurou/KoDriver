package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeTest {

    @Test
    fun `vehicle_approach は VehicleApproach を返す`() {
        assertEquals(ReadoutListItemType.VehicleApproach, ReadoutListItemType.fromId(ReadoutItemKey.VEHICLE_APPROACH))
    }

    @Test
    fun `flag は Flag を返す`() {
        assertEquals(ReadoutListItemType.Flag, ReadoutListItemType.fromId(ReadoutItemKey.FLAG))
    }

    @Test
    fun `vehicle_damage は VehicleDamage を返す`() {
        assertEquals(ReadoutListItemType.VehicleDamage, ReadoutListItemType.fromId(ReadoutItemKey.VEHICLE_DAMAGE))
    }

    @Test
    fun `一覧に含まれないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(ReadoutItemKey.BLUE_FLAG))
    }
}
