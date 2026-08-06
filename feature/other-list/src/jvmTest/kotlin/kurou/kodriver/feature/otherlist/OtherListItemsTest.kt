package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals

class OtherListItemsTest {
    @Test
    fun `nonAndroidではServerIpとKeepScreenOnとDynamicColorを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}
