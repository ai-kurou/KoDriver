@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class OtherListItemsTest {

    @Test
    fun `Android12以上では全項目を定義順で返す`() {
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
                OtherListItemType.DynamicColor,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.License,
            ),
            items,
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OtherListItemsAndroid11Test {

    @Test
    fun `Android12未満ではDynamicColorを除いた全項目を定義順で返す`() {
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
