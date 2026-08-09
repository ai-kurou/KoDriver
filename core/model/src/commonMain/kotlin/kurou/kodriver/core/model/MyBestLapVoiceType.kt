package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MyBestLapVoiceType(
    val id: String,
) {
    FORMAL("formal"),
    CASUAL("casual"),
    ;

    companion object {
        fun fromId(id: String): MyBestLapVoiceType? = entries.firstOrNull { it.id == id }
    }
}
