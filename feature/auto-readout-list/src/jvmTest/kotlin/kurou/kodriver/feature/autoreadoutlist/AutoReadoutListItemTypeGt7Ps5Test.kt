package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertNull

class AutoReadoutListItemTypeGt7Ps5Test {
    @Test
    fun `gt7_ps5 の非 Root キーは null を返す`() {
        assertNull(Gt7Ps5AutoReadoutListItemType.fromId(Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled))
        assertNull(Gt7Ps5AutoReadoutListItemType.fromId(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled))
        assertNull(Gt7Ps5AutoReadoutListItemType.fromId(Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled))
        assertNull(Gt7Ps5AutoReadoutListItemType.fromId(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning))
    }
}
