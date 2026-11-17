package kurou.kodriver.domain.model

public enum class ThemeMode(
    public val id: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    public companion object {
        public fun fromId(id: String): ThemeMode = entries.firstOrNull { it.id == id } ?: THEME_MODE_DEFAULT
    }
}
