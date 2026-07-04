package kurou.kodriver.domain.model

enum class ThemeMode(
    val id: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromId(id: String): ThemeMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}
