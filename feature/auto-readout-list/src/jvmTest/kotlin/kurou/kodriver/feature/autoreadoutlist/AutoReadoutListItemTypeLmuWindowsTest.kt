package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertNull

class AutoReadoutListItemTypeLmuWindowsTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `lmu_windows の非Rootキーは null を返す`() {
        assertNull(LmuWindowsAutoReadoutListItemType.fromId(LmuWindowsReadoutItemKey.Flag.BlueFlag))
    }
}
