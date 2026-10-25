package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherListItemTypeTest {
    @Test
    fun `データログはアプリ設定と情報の間にあり削除項目だけが所属する`() {
        assertEquals(
            listOf(
                OtherListSection.ConnectionSettings,
                OtherListSection.ReadoutSettings,
                OtherListSection.OverlaySettings,
                OtherListSection.AppSettings,
                OtherListSection.DataLog,
                OtherListSection.Information,
            ),
            otherListSections,
        )
        assertEquals(
            listOf(OtherListItemType.DeleteAllLogs),
            OtherListItemType.entries.filter { it.section() == OtherListSection.DataLog },
        )
    }

    @Test
    fun `存在するidを渡すと対応するOtherListItemTypeを返す`() {
        assertEquals("delete_all_logs", OtherListItemType.DeleteAllLogs.id)
        assertEquals(OtherListItemType.DeleteAllLogs, OtherListItemType.fromId("delete_all_logs"))
        assertEquals("overlay_text_color", OtherListItemType.OverlayTextColor.id)
        assertEquals(OtherListItemType.OverlayTextColor, OtherListItemType.fromId("overlay_text_color"))
        assertEquals(OtherListItemType.Volume, OtherListItemType.fromId("volume"))
        assertEquals(OtherListItemType.Voice, OtherListItemType.fromId("voice"))
        assertEquals(OtherListItemType.VoiceSpeed, OtherListItemType.fromId("voice_speed"))
        assertEquals(OtherListItemType.VoicePitch, OtherListItemType.fromId("voice_pitch"))
        assertEquals(OtherListItemType.AndroidAudioUsage, OtherListItemType.fromId("android_audio_usage"))
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

    @Test
    fun `entriesは全27項目を宣言順で返す`() {
        assertEquals(27, OtherListItemType.entries.size)
        assertEquals(
            listOf(
                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.WindowsSpeechUnavailable,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.VoiceSpeed,
                OtherListItemType.VoicePitch,
                OtherListItemType.AndroidAudioUsage,
                OtherListItemType.Theme,
                OtherListItemType.OverlayVisible,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayTextColor,
                OtherListItemType.OverlayBackgroundOpacity,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.Startup,
                OtherListItemType.DeleteAllLogs,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
                OtherListItemType.DebugState,
            ),
            OtherListItemType.entries,
        )
        assertEquals(
            listOf(
                "access_local_network_permission",
                "server_ip",
                "console_ip",
                "volume",
                "tts_engine_missing",
                "tts_language_data_missing",
                "windows_speech_unavailable",
                "keep_screen_on",
                "readout_start_sound",
                "voice",
                "voice_speed",
                "voice_pitch",
                "android_audio_usage",
                "theme",
                "overlay_visible",
                "overlay_text_size",
                "overlay_text_color",
                "overlay_background_opacity",
                "dynamic_color",
                "haptic_feedback",
                "startup",
                "delete_all_logs",
                "github_repository",
                "release_page",
                "feedback",
                "license",
                "debug_state",
            ),
            OtherListItemType.entries.map { it.id },
        )
    }

    @Test
    fun `ReadoutSettingsには対応する9項目だけが所属する`() {
        assertEquals(
            setOf(
                OtherListItemType.Volume,
                OtherListItemType.Voice,
                OtherListItemType.VoiceSpeed,
                OtherListItemType.VoicePitch,
                OtherListItemType.AndroidAudioUsage,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.WindowsSpeechUnavailable,
            ),
            OtherListItemType.entries.filterIsInstance<OtherListItemType.ReadoutSettings>().toSet(),
        )
    }

    @Test
    fun `OverlaySettingsには対応する4項目だけが所属する`() {
        assertEquals(
            setOf(
                OtherListItemType.OverlayVisible,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayTextColor,
                OtherListItemType.OverlayBackgroundOpacity,
            ),
            OtherListItemType.entries.filterIsInstance<OtherListItemType.OverlaySettings>().toSet(),
        )
    }

    @Test
    fun `AppSettingsには対応する5項目だけが所属する`() {
        assertEquals(
            setOf(
                OtherListItemType.KeepScreenOn,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.Startup,
            ),
            OtherListItemType.entries.filterIsInstance<OtherListItemType.AppSettings>().toSet(),
        )
    }

    @Test
    fun `entriesはOtherListItemTypeに宣言された全てのdata objectを含む`() {
        val declaredObjects =
            OtherListItemType::class.java.declaredClasses
                .filterNot { it.isInterface }
                .mapNotNull { runCatching { it.getField("INSTANCE").get(null) }.getOrNull() }
                .filterIsInstance<OtherListItemType>()

        assertEquals(declaredObjects.toSet(), OtherListItemType.entries.toSet())
        assertEquals(declaredObjects.size, OtherListItemType.entries.size)
    }
}
