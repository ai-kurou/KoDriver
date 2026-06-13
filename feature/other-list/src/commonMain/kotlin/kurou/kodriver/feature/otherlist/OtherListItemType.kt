package kurou.kodriver.feature.otherlist

enum class OtherListItemType(
    val id: String,
) {
    Volume("volume"),
    GitHubRepository("github_repository"),
    ReleasePage("release_page"),
    License("license"),
    ;

    companion object {
        fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
