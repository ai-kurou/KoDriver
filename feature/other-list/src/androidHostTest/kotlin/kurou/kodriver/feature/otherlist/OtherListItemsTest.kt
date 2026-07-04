@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertContains

class OtherListItemsTest {

    @Test
    fun `ServerIpを含む`() {
        val items = buildOtherListItems()

        assertContains(items, OtherListItemType.ServerIp)
    }

    @Test
    fun `KeepScreenOnを含む`() {
        val items = buildOtherListItems()

        assertContains(items, OtherListItemType.KeepScreenOn)
    }

    @Test
    fun `ConsoleIpを含む`() {
        val items = buildOtherListItems()

        assertContains(items, OtherListItemType.ConsoleIp)
    }

    @Test
    fun `Themeを含む`() {
        val items = buildOtherListItems()

        assertContains(items, OtherListItemType.Theme)
    }
}
