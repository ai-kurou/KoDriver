package kurou.kodriver.domain.model

enum class OverlayTextSize(
    val id: String,
) {
    EXTRA_SMALL("extra_small"),
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    EXTRA_LARGE("extra_large"),
    HUGE("huge"),
    MAXIMUM("maximum"),
    ;

    companion object {
        fun fromId(id: String): OverlayTextSize = entries.firstOrNull { it.id == id } ?: OVERLAY_TEXT_SIZE_DEFAULT
    }
}
