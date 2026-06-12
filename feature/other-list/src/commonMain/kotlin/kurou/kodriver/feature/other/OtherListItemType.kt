package kurou.kodriver.feature.other

enum class OtherListItemType(
    val id: String,
) {
    GitHubRepository("github_repository"),
    ReleasePage("release_page"),
    License("license"),
    ;

    companion object {
        fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
