package kurou.kodriver.domain.model

enum class OverlayTextSize(
    val id: String,
) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    ;

    companion object {
        fun fromId(id: String): OverlayTextSize = entries.firstOrNull { it.id == id } ?: OVERLAY_TEXT_SIZE_DEFAULT
    }
}
