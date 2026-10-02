package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtherListItemsTest {
    @Test
    fun `nonAndroidではAccessLocalNetworkPermissionとServerIpとKeepScreenOnとDynamicColorとHapticFeedbackを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.Theme,
                OtherListItemType.OverlayVisible,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayBackgroundOpacity,
                OtherListItemType.Startup,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }

    @Test
    fun `デスクトップでは読み上げ音声を一覧に含める`() {
        assertTrue(buildOtherListItems().contains(OtherListItemType.Voice))
    }
}
