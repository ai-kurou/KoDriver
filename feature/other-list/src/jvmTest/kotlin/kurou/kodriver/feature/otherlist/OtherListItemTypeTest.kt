package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListItemTypeTest {
    @Test
    fun `存在するidを渡すと対応するOtherListItemTypeを返す`() {
        assertEquals(OtherListItemType.Volume, OtherListItemType.fromId("volume"))
        assertEquals(OtherListItemType.Voice, OtherListItemType.fromId("voice"))
        assertEquals(OtherListItemType.VoiceSpeed, OtherListItemType.fromId("voice_speed"))
    }

    @Test
    fun `存在しないidを渡すとnullを返す`() {
        assertNull(OtherListItemType.fromId("unknown"))
    }

    @Test
    fun `全項目のidは一意で種別に復元できる`() {
        assertEquals(
            OtherListItemType.entries.size,
            OtherListItemType.entries
                .map { it.id }
                .distinct()
                .size,
        )
        OtherListItemType.entries.forEach { assertEquals(it, OtherListItemType.fromId(it.id)) }
    }
}
