package kurou.kodriver.domain.model

enum class RedFlagVoiceType(
    val id: String,
) {
    RED_FLAG("red_flag"),
    SESSION_STOP("session_stop"),
    ;

    companion object {
        fun fromId(id: String): RedFlagVoiceType =
            entries.firstOrNull { it.id == id } ?: SESSION_STOP
    }
}
