package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class OtherListItemsTest {

    @Test
    fun `ServerIpを含まない`() {
        val items = buildOtherListItems()

        assertFalse(items.contains(OtherListItemType.ServerIp))
    }

    @Test
    fun `ConsoleIpを含む`() {
        val items = buildOtherListItems()

        assertContains(items, OtherListItemType.ConsoleIp)
    }
}
