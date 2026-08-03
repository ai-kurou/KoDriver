package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MyBestLapVoiceType(
    val id: String,
) {
    FORMAL("formal"),
    CASUAL("casual"),
    ;

    companion object {
        fun fromId(id: String): MyBestLapVoiceType =
            entries.firstOrNull { it.id == id } ?: MY_BEST_LAP_VOICE_TYPE_DEFAULT
    }
}
