package kurou.kodriver.feature.otherlist

enum class OtherListItemType(
    val id: String,
) {
    ServerIp("server_ip"),
    ConsoleIp("console_ip"),
    Volume("volume"),
    ReadoutStartSound("readout_start_sound"),
    GitHubRepository("github_repository"),
    ReleasePage("release_page"),
    License("license"),
    ;

    companion object {
        fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
