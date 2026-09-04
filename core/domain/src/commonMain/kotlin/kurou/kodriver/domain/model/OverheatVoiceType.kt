package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class OverheatVoiceType(
    val id: String,
) {
    GP2_GP2("gp2_gp2"),
    STANDARD("standard"),
    ;

    companion object {
        fun fromId(id: String): OverheatVoiceType = entries.firstOrNull { it.id == id } ?: OVERHEAT_VOICE_TYPE_DEFAULT
    }
}
