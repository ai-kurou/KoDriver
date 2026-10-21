package kurou.kodriver.domain.model

enum class AndroidAudioUsage(
    val id: String,
) {
    MEDIA("media"),
    COMMUNICATION("communication"),
    RINGTONE("ringtone"),
    NOTIFICATION("notification"),
    ALARM("alarm"),
    ASSISTANT("assistant"),
    ;

    companion object {
        fun fromId(id: String): AndroidAudioUsage = entries.firstOrNull { it.id == id } ?: ANDROID_AUDIO_USAGE_DEFAULT
    }
}
