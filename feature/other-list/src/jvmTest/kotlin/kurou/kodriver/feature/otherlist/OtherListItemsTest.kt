package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtherListItemsTest {
    @Test
    fun `文字色は文字サイズの直後かつ背景透明度の前に表示する`() {
        val items = buildOtherListItems()
        val textSizeIndex = items.indexOf(OtherListItemType.OverlayTextSize)

        assertEquals(OtherListItemType.OverlayTextColor, items[textSizeIndex + 1])
        assertEquals(OtherListItemType.OverlayBackgroundOpacity, items[textSizeIndex + 2])
    }

    @Test
    fun `声の高さを読み上げ設定の最下部に含める`() {
        val items = buildOtherListItems()

        assertEquals(OtherListItemType.VoicePitch, items[items.indexOf(OtherListItemType.VoiceSpeed) + 1])
        assertEquals(OtherListItemType.Theme, items[items.indexOf(OtherListItemType.VoicePitch) + 1])
    }

    @Test
    fun `読み上げ速度を読み上げ音声の直後に含める`() {
        val items = buildOtherListItems()

        assertEquals(OtherListItemType.VoiceSpeed, items[items.indexOf(OtherListItemType.Voice) + 1])
    }

    @Test
    fun `nonAndroidではAccessLocalNetworkPermissionとServerIpとKeepScreenOnとDynamicColorとHapticFeedbackを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.VoiceSpeed,
                OtherListItemType.VoicePitch,
                OtherListItemType.Theme,
                OtherListItemType.OverlayVisible,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayTextColor,
                OtherListItemType.OverlayBackgroundOpacity,
                OtherListItemType.Startup,
                OtherListItemType.DeleteAllLogs,
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

    @Test
    fun `デスクトップではAndroidの音声用途を一覧に含めない`() {
        assertTrue(OtherListItemType.AndroidAudioUsage !in buildOtherListItems())
    }
}
