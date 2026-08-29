@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListItemsTest {
    @Test
    fun `Android16以上では全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherListItemsAndroid15Test {
    @Test
    fun `Android16未満ではAccessLocalNetworkPermissionを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
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
    fun `Android12未満ではDynamicColorとAccessLocalNetworkPermissionを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}
