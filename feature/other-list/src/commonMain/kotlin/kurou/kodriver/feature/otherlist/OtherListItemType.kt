package kurou.kodriver.feature.otherlist

/**
 * OtherList の項目種別。
 */
enum class OtherListItemType(
    val id: String,
) {
    AccessLocalNetworkPermission("access_local_network_permission"),
    ServerIp("server_ip"),
    ConsoleIp("console_ip"),
    Volume("volume"),
    TtsEngineMissing("tts_engine_missing"),
    TtsLanguageDataMissing("tts_language_data_missing"),
    WindowsSpeechUnavailable("windows_speech_unavailable"),
    KeepScreenOn("keep_screen_on"),
    ReadoutStartSound("readout_start_sound"),
    Voice("voice"),
    VoiceSpeed("voice_speed"),
    Theme("theme"),
    OverlayVisible("overlay_visible"),
    OverlayTextSize("overlay_text_size"),
    OverlayBackgroundOpacity("overlay_background_opacity"),
    DynamicColor("dynamic_color"),
    HapticFeedback("haptic_feedback"),
    Startup("startup"),
    GitHubRepository("github_repository"),
    ReleasePage("release_page"),
    Feedback("feedback"),
    License("license"),
    DebugState("debug_state"),
    ;

    companion object {
        fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
