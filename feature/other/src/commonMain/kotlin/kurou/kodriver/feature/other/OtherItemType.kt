package kurou.kodriver.feature.other

enum class OtherItemType(
    val id: String,
) {
    License("license"),
    ;

    companion object {
        fun fromId(id: String): OtherItemType? = entries.firstOrNull { it.id == id }
    }
}
