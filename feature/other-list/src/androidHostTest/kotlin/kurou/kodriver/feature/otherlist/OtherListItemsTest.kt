@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals

class OtherListItemsTest {

    @Test
    fun `Androidでは全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.ExitConfirmation,
                OtherListItemType.Theme,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.License,
            ),
            items,
        )
    }
}
