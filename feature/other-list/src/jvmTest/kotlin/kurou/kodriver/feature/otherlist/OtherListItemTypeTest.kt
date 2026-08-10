package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListItemTypeTest {
    @Test
    fun `存在するidを渡すと対応するOtherListItemTypeを返す`() {
        assertEquals(OtherListItemType.Volume, OtherListItemType.fromId("volume"))
    }

    @Test
    fun `存在しないidを渡すとnullを返す`() {
        assertNull(OtherListItemType.fromId("unknown"))
    }
}
