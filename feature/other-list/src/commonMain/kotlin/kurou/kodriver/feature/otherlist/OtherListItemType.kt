package kurou.kodriver.feature.otherlist

/**
 * OtherList の項目種別。
 */
enum class OtherListItemType(
    val id: String,
) {
    ServerIp("server_ip"),
    ConsoleIp("console_ip"),
    Volume("volume"),
    KeepScreenOn("keep_screen_on"),
    ReadoutStartSound("readout_start_sound"),
    Theme("theme"),
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
