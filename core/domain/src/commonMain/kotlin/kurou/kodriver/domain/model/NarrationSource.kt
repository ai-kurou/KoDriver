package kurou.kodriver.domain.model

/** テレメトリログに保存する読み上げの起点を表す。 */
enum class NarrationSource(
    val id: String,
) {
    /** 自動読み上げ。 */
    AUTO("auto"),

    /** 手動読み上げ。 */
    MANUAL("manual"),
    ;

    companion object {
        fun fromId(id: String): NarrationSource? = entries.firstOrNull { it.id == id }
    }
}
